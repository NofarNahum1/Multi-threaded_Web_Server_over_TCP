import java.io.*;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class HTTPStatusCode {
    static String notFound = "404 Not Found";
    static String ok = "200 OK";
    static String notImplemented = "501 Not Implemented";
    static String badRequest = "400 Bad Request";
    static String InternalServer = "500 Internal Server Error";

    public void HTTPErrorCodeResponse(OutputStream out, String error) {
        PrintWriter pout = new PrintWriter(out);
        String header1 = "HTTP/1.1 " + error + "\r\n";
        String header2 = "Content-Type: text/html\r\n";
        String header3 = "Content-length: 0\r\n";
        String header4 = "Connection: close\r\n";

        pout.print(header1);
        pout.print(header2);
        pout.print(header3);
        pout.print(header4);
        pout.print("\r\n");
        pout.flush();
        pout.close();

        System.out.println("\n" + "server response: " + "\n" + header1 + header2 + header3 + header4 + "\r\n");
    }

    public void HTTP200OkResponse(OutputStream out, byte[] bodyContent, int contentLength, boolean isChunked, String contentType)  {
        String header1 = "HTTP/1.1 " + ok + "\r\n";
        String header2 = "Content-Type: " + contentType + "\r\n";
        String header3 = "";

        PrintWriter pout = new PrintWriter(out);
        pout.print(header1);
        pout.print(header2);

        if (isChunked){
            header3 = "Transfer-encoding: chunked\r\n";
            pout.print(header3);
            pout.print("\r\n");
        }
        else {
            header3 = "Content-length: " + contentLength + "\r\n";
            pout.print(header3);
            pout.print("\r\n");
            // need to restrict somehow the size of the body content - say when a huge image sent the server is crushing trying to send the image contenet.
            pout.print(new String(bodyContent, StandardCharsets.UTF_8)); // return the client the content of the file\image
        }
        pout.flush();
        System.out.println("\n" + "server response: " + "\n"  + header1 + header2 + header3 + "\r\n");
    }

    public void HTTPHeadOkResponse(OutputStream out, byte[] bodyContent, int contentLength, boolean isChunked, String contentType)  {
        String header1 = "HTTP/1.1 " + ok + "\r\n";
        String header2 = "Content-Type: " + contentType + "\r\n";
        String header3 = "";

        PrintWriter pout = new PrintWriter(out);
        pout.print(header1);
        pout.print(header2);

        header3 = "Content-length: " + contentLength + "\r\n";
        pout.print(header3);
        pout.print("\r\n");

        pout.flush();
        System.out.println("\n" + "server response: " + "\n"  + header1 + header2 + header3 + "\r\n");
    }

    public void HTTPTraceOkResponse(OutputStream out,  StringBuilder clientRequest , int contentLength ) {

        String header1 = "HTTP/1.1 " + ok + "\r\n";
        String header2 = "Connection: close\r\n";
        String header3 = "Content-Type: message/html\r\n";
        String header4 = "Content length: " + contentLength + "\r\n";

        PrintWriter pout = new PrintWriter(out);
        pout.print(header1);
        pout.print(header2);
        pout.print(header3);
        pout.print(header4);
        pout.print("\r\n");

        pout.print(clientRequest.toString()); // returns the client the request

        pout.flush();
        System.out.println("\n" + "server response: " + "\n"  + header1 + header2 + header3 + header4 + "\r\n" +  clientRequest + "\r\n");
    }
}
