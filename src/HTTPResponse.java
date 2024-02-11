import java.io.File;
import java.io.IOException;

public class HTTPResponse {
    private String contentType;
    private int responseCode;
    private File requestedFile;
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

            if (request.getPath().equals("")) {
                requestedFile = new File(Server.config.getRoot(), Server.config.getDefaultPage());
                responseCode = 200;
            } else {
                requestedFile = new File(Server.config.getRoot(), request.getPath());
                if (requestedFile.exists()) {
                    String canonicalPath = requestedFile.getCanonicalPath();
                    if (canonicalPath.startsWith(Server.config.getRoot())) {
                        responseCode = 200;
                    } else {
                        System.out.println("Bad path passed sanitization: " + request.getPath());
                        responseCode = 400;
                    }
                } else {
                    responseCode = 404;
                }
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

    public String buildHeaders() {
        return "HTTP/1.1 " + responseCodeToString() + "\r\n" + contentType +
                "\r\ncontent-length: " + requestedFile.length() + "\r\n\r\n";
    }

    private String responseCodeToString() {
        switch (responseCode) {
            case 200:
                return "200 OK";
            case 400:
                return "400 Bad Request";
            case 404:
                return "404 Not Found";
            case 501:
                return "501 Not Implemented";
            default:
                return "500 Internal Server Error";
        }
    }

    public File getFile() {
        return requestedFile;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
