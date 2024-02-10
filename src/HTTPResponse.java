import java.io.File;
import java.io.IOException;

public class HTTPResponse {
    private String contentType;
    private int responseCode;
    public HTTPResponse(HTTPRequest request) throws IOException {
        if (request.getMethod() == HTTPRequest.Method.GET) {
            if (request.getIsHTML()) {
                contentType = "content-type: text/html";
            } else if (request.getIsImage()) {
                contentType = "content-type: image";
            } else if (request.getIsIcon()) {
                contentType = "content-type: icon";
            } else {
                contentType = "content-type: application/octet-stream";
            }

            File file = new File(Server.config.getRoot() + request.getPath());
            if (file.exists()) {
                String canonicalPath = file.getCanonicalPath();
                if (canonicalPath.startsWith(Server.config.getRoot().substring((1)))) {
                    System.out.println("Sending 200");
                    responseCode = 200;
                }
                else {
                    System.out.println("Bad path passed sanitization: " + request.getPath());
                    responseCode = 400;
                }
            }
            else {
                System.out.println("Sending 404");
                responseCode = 404;
            }

        }
        else if (request.getMethod() == HTTPRequest.Method.POST) {
            System.out.println("Sending 404");
            responseCode = 404;
        }
        else {
            System.out.println("Sending 501");
            responseCode = 501;
        }
    }
}
