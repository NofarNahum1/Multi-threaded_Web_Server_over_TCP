import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class HTTPRequest {
    String requestType;
    String requestedPage;
    boolean requestedFileIsImage = false;
    int contentLength;
    String referer;
    String userAgent;
    HashMap<String, String> urlParameters = new HashMap<String, String>();
    boolean isChunked;
    boolean isIcon = false;
    StringBuilder requestHeaders = new StringBuilder();
    StringBuilder requestBody = new StringBuilder();

    public HTTPRequest(StringBuilder requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public void ParseRequestHeaders(){
        // each line is a header
        String[] lines = requestHeaders.toString().split("\n");
        boolean firstLine = true;

        for (String line:lines) {
            if (firstLine){
                firstLine = false;
                String[] tokens = line.split(" ");
                requestType = tokens[0];
                requestedPage = tokens[1];

                String[] pageTokens = tokens[1].split("\\?"); // separating between page and params.
                if (pageTokens.length > 1){ // if the number of elements in pageToken is bigger than 1 then there are params.
                    requestedPage = pageTokens[0];
                    String[] params = pageTokens[1].split("&");
                    for (String param: params){
                        String key = param.split("=")[0];
                        String value = param.split("=")[1];
                        urlParameters.put(key,value);
                    }
                    System.out.println("The parameters in the request line are: " + urlParameters.toString()); // trace for params
                }

                if (requestedPage.split("\\.").length >= 2) { // separating between page and page extension.
                    String pageExtension = requestedPage.split("\\.")[1];
                    requestedFileIsImage = switch (pageExtension) {
                        case "bmp", "gif", "png", "jpg" -> true;
                        default -> false;
                    };
                    if (pageExtension.equals("ico")){
                        isIcon = true;
                    }
                }
            }

            String[] headerSection = line.split(":");
            switch (headerSection[0]){
                case "Content-Length":
                    contentLength = Integer.parseInt(headerSection[1].replaceAll(" ",""));
                    break;
                case "Referer":
                    referer = headerSection[1];
                    break;
                case "User-Agent":
                    userAgent = headerSection[1];
                    break;
                case "Chunked":
                    isChunked = headerSection[1].replaceAll(" ", "").equals("yes");
                default:
                    break;
            }
        }
    }

    // parsing the body parameters
    public Map<String, String> parseBodyToMap(String body, Map<String, String> urlParameters) throws UnsupportedEncodingException {
        Map<String, String> parameters = new LinkedHashMap<>(urlParameters);
        //Map<String, String> parameters = new HashMap<>();

        if (body != null && !body.isEmpty()) {
            String[] lines = body.split("\n");

            for (String line : lines) { // message=YourMessage&checkbox=true
                // Split the body into individual key-value pairs
                String[] keyValuePairs = line.split("&"); // keyValuePairs = ["message=YourMessage", "checkbox=true"]

                // Parse each key-value pair and add to the map
                for (String pair : keyValuePairs) {
                    String[] entry = pair.split("="); // entry = [message, yourMessage], and in the 2nd iteration: = [checkbox,true]

                    // checks whether the entry array has exactly two elements
                    if (entry.length == 2) {
                        String key = entry[0];
                        String value = entry[1];
                        parameters.put(key, value);
                    }
                }
            }
        }

        return parameters;
    }

    public String toString(){
        StringBuilder res = new StringBuilder("\n" + "Type: " + requestType + "\n" + "page requested: " + requestedPage + "\n" + "is image: " +
                requestedFileIsImage + "\n" + "length: " + contentLength + "\n" + "referer: " + referer + "\n" +
                "user agent: " + userAgent + "\n" + "Chunked: " + isChunked + "\n");
        for(Map.Entry<String,String> entry : urlParameters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            res.append("key: ").append(key).append(",value: ").append(value).append("\n");
        }
        return res.toString();
    }
}
