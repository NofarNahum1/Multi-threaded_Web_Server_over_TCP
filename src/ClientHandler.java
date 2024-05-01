import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final String rootDirectory;
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

    public ClientHandler(Socket clientSocket, String root) {
        this.clientSocket = clientSocket;
        this.rootDirectory = root;
    }

    @Override
    public void run() {
        BufferedReader in = null;
        OutputStream out = null;
        PrintWriter pout = null;

        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // will be used for reading data from the client.
            out = clientSocket.getOutputStream(); // will be used for sending data back to the client.
            pout = new PrintWriter(out); // used to write data to the output stream, provides methods to write formatted text.

            String requestLine = in.readLine(); // first line of the request
            StringBuilder requestHeaders = new StringBuilder();
            StringBuilder requestBody = new StringBuilder();

            // checks if we arrived to an empty line which is the end of the headers
            while (requestLine != null && !requestLine.isEmpty()) {
                requestHeaders.append(requestLine).append("\n");
                requestLine = in.readLine();
            }
            ;

            // sending the request line & the headers of the request to the HTTPRequest constructor.
            HTTPRequest httpRequest = new HTTPRequest(requestHeaders);
            httpRequest.ParseRequestHeaders(); // parsing the request message.
            System.out.println("\n" + "Client request is:" + "\n" + requestHeaders); // printing the parsed client request

            // separate the body from the request
            if (httpRequest.contentLength > 0) {
                requestLine = in.readLine();

                while (requestLine != null && !requestLine.isEmpty()) {
                    requestBody.append(requestLine).append("\n");
                    requestLine = in.readLine();
                }

            }

            // validate request type (if the method used is unknown), if not valid return 501
            switch (httpRequest.requestType) {
                case "GET":
                    serveFile(httpRequest.requestedPage, out, httpRequest.isChunked); // handling the GET request
                    break;

                case "POST":
                    if (httpRequest.contentLength > 0) {
                        serveFile(httpRequest.requestedPage, out, httpRequest.isChunked);

                        // Parse HTML page to extract parameters
                        Map<String, String> parameters = httpRequest.parseBodyToMap(requestBody.toString(), httpRequest.urlParameters);

                        if (!parameters.isEmpty()){
                            System.out.println("The request body parameters are: ");
                            parameters.forEach((key, value) -> System.out.println(key + ": " + value));
                        }
                    }

                    break;

                case "HEAD": // used to request the headers of a resource, not the actual content. send back just the response headers
                    String contentType = getContentType(httpRequest.requestedPage);
                    serveHeadersOnly(httpRequest.requestedPage, out, contentType);
                    break;

                case "TRACE":
                    // send back the request to the client
                    serveTrace(out, requestHeaders);
                    break;

                default:
                    // Handling other HTTP methods that are not implemented
                    HTTPStatusCode status = new HTTPStatusCode();
                    status.HTTPErrorCodeResponse(out, HTTPStatusCode.notImplemented);
                    break;
            }
        } catch (IOException e) {
            logger.severe("Bad Request Error: " + e.getMessage());
            logger.log(Level.SEVERE, "Exception stack trace", e);

            // Send a 500 Internal Server Error response to the client
            HTTPStatusCode status = new HTTPStatusCode();
            status.HTTPErrorCodeResponse(out, HTTPStatusCode.badRequest);

        } finally {
            try {
                if (pout != null) pout.close();
                if (out != null) out.close();
                if (in != null) in.close();
            } catch (IOException e) {
                logger.severe("Internal Server Error: " + e.getMessage());
                logger.log(Level.SEVERE, "Exception stack trace", e);

                HTTPStatusCode status = new HTTPStatusCode();
                status.HTTPErrorCodeResponse(out, HTTPStatusCode.InternalServer);
            }
        }
    }

    private void serveFile(String fileRequested, OutputStream out, boolean chunked) throws IOException {
        Path rootPath = Paths.get(rootDirectory); // rootDirectory converted into a Path object.
        Path resolvedPath = rootPath.resolve(fileRequested.substring(1)).normalize(); // Remove the leading '/' from fileRequested & combine the root path abd the file requested to one path.
        File file = resolvedPath.toFile();
        HTTPStatusCode responseStatus = new HTTPStatusCode();

        // traces
        System.out.println("The root path: " + rootPath);
        System.out.println("The file requested: " + fileRequested);
        System.out.println("the resolved path: " + resolvedPath);


        // if file does not found - return 404 error
        if (!file.exists() || file.isDirectory() || !(resolvedPath.startsWith(rootPath))) {
            responseStatus.HTTPErrorCodeResponse(out, HTTPStatusCode.notFound);

            return;
        }

        String contentType = getContentType(fileRequested); // type of the file requested
        byte[] fileData = Files.readAllBytes(resolvedPath); // read the entire contents of a file into a byte array.
        PrintWriter pout = new PrintWriter(out); // send each line at the moment she finished entering the output buffer.

        if (chunked) {
            // Send the HTTP headers indicating chunked transfer encoding.
            responseStatus.HTTP200OkResponse(out, null, 0, true, contentType);

            try (FileInputStream fis = new FileInputStream(file)) { // create an input stream for reading bytes from the requested file
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];
                int bytesRead;
                boolean b = true;

                // Continue reading until the end of the file is reached
                while (b && (bytesRead = fis.read(buffer)) != -1) {
                    out.write(Integer.toHexString(bytesRead).getBytes());
                    out.write("\r\n".getBytes());
                    out.write(buffer, 0, bytesRead);
                    out.write("\r\n".getBytes());
                    if (bytesRead < bufferSize) {
                        b = false;
                    }
                }
                out.write("0\r\n\r\n".getBytes());
                out.flush();
            } catch (IOException e) {
                logger.severe("IOException occurred: " + e.getMessage());
                logger.log(Level.SEVERE, "Exception stack trace", e);
            }
        } else {
            // System.out.println("in clientHandler line 158, fileData.len = " + fileData.length); remove!!!
            responseStatus.HTTP200OkResponse(out, fileData, fileData.length, false, contentType);
        }
    }

    public String getContentType(String fileRequested) {  // Image files supported: .bmp, .gif, .png, .jpg.
        if (fileRequested.endsWith(".htm") || fileRequested.endsWith(".html")) {
            return "text/html";
        } else if (fileRequested.endsWith(".bmp")) {
            return "image/bmp";
        } else if (fileRequested.endsWith(".jpg") || fileRequested.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileRequested.endsWith(".gif")) {
            return "image/gif";
        } else if (fileRequested.endsWith(".png")) {
            return "image/png";
        } else if (fileRequested.endsWith(".ico")) { // need to check if this is ok- test !
            return "icon";
        } else {
            return "application/octet-stream";
        }
    }

    private void serveHeadersOnly(String fileRequested, OutputStream out, String contentType) throws IOException {
        Path rootPath = Paths.get(rootDirectory);
        Path resolvedPath = rootPath.resolve(fileRequested.substring(1)).normalize();
        HTTPStatusCode status = new HTTPStatusCode();


        if (!Files.exists(resolvedPath) || Files.isDirectory(resolvedPath)) {
            status.HTTPErrorCodeResponse(out, HTTPStatusCode.notFound);
            return;
        }

        byte[] fileData = Files.readAllBytes(resolvedPath);

        status.HTTPHeadOkResponse(out, fileData, fileData.length, false, contentType);
    }

    private void serveTrace(OutputStream out, StringBuilder requestHeaders) throws IOException {
        HTTPStatusCode status = new HTTPStatusCode();
        HTTPRequest httpRequest = new HTTPRequest(requestHeaders);
        httpRequest.ParseRequestHeaders();
        httpRequest.contentLength = requestHeaders.toString().getBytes().length;; // in trace: Content-Length header indicates the length of the echoed request in the response body.

        Path rootPath = Paths.get(rootDirectory); // rootDirectory converted into a Path object.
        Path resolvedPath = rootPath.resolve(httpRequest.requestedPage.substring(1)).normalize(); // Remove the leading '/' from fileRequested & combine the root path abd the file requested to one path.
        File file = resolvedPath.toFile();

        // traces
        System.out.println("The file requested: " + httpRequest.requestedPage);
        System.out.println("the resolved path: " + resolvedPath);


        // if file does not found - return 404 error
        if (!file.exists() || file.isDirectory() || !(resolvedPath.startsWith(rootPath))) {
            status.HTTPErrorCodeResponse(out, HTTPStatusCode.notFound);

            return;
        }
        status.HTTPTraceOkResponse(out, requestHeaders, httpRequest.contentLength);
    }
}
