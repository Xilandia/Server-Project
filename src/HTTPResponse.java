import java.io.File;
import java.io.IOException;

public class HTTPResponse {
    private String contentType;
    private int responseCode;
    private File requestedFile;
    private int contentLength;
    private boolean sendDecision = false;
    public HTTPResponse(HTTPRequest request) throws IOException {
        if (request.getMethod() == HTTPRequest.Method.GET || request.getMethod() == HTTPRequest.Method.HEAD || request.getMethod() == HTTPRequest.Method.POST) {
            if (request.getIsHTML()) {
                contentType = "content-type: text/html";
            } else if (request.getIsImage()) {
                contentType = "content-type: image";
            } else if (request.getIsIcon()) {
                contentType = "content-type: icon";
            } else if (request.getIsCSS()) {
                contentType = "content-type: text/css";
            } else {
                contentType = "content-type: application/octet-stream";
            }

            if (request.getPath().isEmpty()) {
                requestedFile = new File(Server.config.getRoot(), Server.config.getDefaultPage());
                contentLength = (int) requestedFile.length();
                responseCode = 200;
                if (request.getMethod() != HTTPRequest.Method.HEAD) {
                    sendDecision = true;
                }
            } else {
                requestedFile = new File(Server.config.getRoot(), request.getPath());
                contentLength = (int) requestedFile.length();
                if (requestedFile.exists()) {
                    String canonicalPath = requestedFile.getCanonicalPath();
                    if (canonicalPath.startsWith(Server.config.getRoot())) {
                        responseCode = 200;
                        if (request.getMethod() != HTTPRequest.Method.HEAD) {
                            sendDecision = true;
                        }
                    } else {
                        System.out.println("Bad path passed sanitization: " + request.getPath());
                        responseCode = 400;
                    }
                } else {
                    requestedFile = new File(Server.config.getRoot(), "404.html");
                    if (requestedFile.exists()) {
                        contentLength = (int) requestedFile.length();
                        responseCode = 404;
                        contentType = "content-type: text/html";
                        if (request.getMethod() != HTTPRequest.Method.HEAD) {
                            sendDecision = true;
                        }
                    } else {
                        System.out.println("404.html not found");
                        responseCode = 500;
                    }
                }
            }
        }
        else if (request.getMethod() == HTTPRequest.Method.TRACE) {
            contentType = "content-type: message/http";
            responseCode = 200;
            contentLength = request.getRequestForTrace().length;
        }
        else {
            System.out.println("Sending 501, method not implemented");
            responseCode = 501;
        }
    }

    public String buildHeaders() {
        return "HTTP/1.1 " + responseCodeToString() + "\r\n" + contentType +
                "\r\ncontent-length: " + contentLength + "\r\n\r\n";
    }

    public String buildChunkedHeaders() {
        return "HTTP/1.1 " + responseCodeToString() + "\r\n" + contentType +
                "\r\ntransfer-encoding: chunked\r\n\r\n";
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

    public int getContentLength() {
        return contentLength;
    }

    public boolean getSendDecision() {
        return sendDecision;
    }
}
