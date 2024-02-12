import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class HTTPRequest {
    public enum Method {
        GET, POST, HEAD, PUT, DELETE, CONNECT, OPTIONS, TRACE, PATCH
    }

    private Method method;
    private String path;
    private boolean isHTML;
    private boolean isImage;
    private boolean isIcon;
    private boolean isCSS;
    private String referer;
    private String userAgent;
    private byte[] requestForTrace;
    private static HashMap<String, String> parameters = new HashMap<>();

    public HTTPRequest(String request) {
        parseRequest(request);
    }

    private void parseRequest(String request) {
        String[] lines = request.split("\n");
        String[] firstLine = lines[0].split(" ");
        String method = firstLine[0];
        String path = SanitizePath(firstLine[1]);
        this.method = Method.valueOf(method);
        this.path = path;
        this.isHTML = path.endsWith(".html") || path.endsWith(".htm") || path.equals("/");
        this.isImage = path.endsWith(".bmp") || path.endsWith(".gif") || path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg"); // bonus - want to support jpeg bc why not
        this.isIcon = path.endsWith(".ico");
        this.isCSS = path.endsWith(".css");
        for (int i = 1; i < lines.length; i++) {
            String[] parts = lines[i].split(": ");
            if (parts[0].equals("Referer")) {
                this.referer = parts[1];
            } else if (parts[0].equals("User-Agent")) {
                this.userAgent = parts[1];
            }
        }

        if (this.method == Method.TRACE) {
            this.requestForTrace = request.getBytes(StandardCharsets.UTF_8);
        }
        if (this.method == Method.POST) {
            int emptyLineIndex = request.indexOf("\r\n\r\n");
            if (emptyLineIndex != -1) {
                String body = request.substring(emptyLineIndex).trim();
                parseParameters(body);
            }
        }

        /*System.out.println("Method: " + this.method);
        System.out.println("Path: " + this.path);
        System.out.println("Is HTML: " + this.isHTML);
        System.out.println("Is Image: " + this.isImage);
        System.out.println("Is Icon: " + this.isIcon);
        System.out.println("Referer: " + this.referer);
        System.out.println("User-Agent: " + this.userAgent);*/
    }

    public Method getMethod() {
        return this.method;
    }

    public String getPath() {
        return this.path;
    }

    public boolean getIsHTML() {
        return this.isHTML;
    }

    public boolean getIsImage() {
        return this.isImage;
    }

    public boolean getIsIcon() {
        return this.isIcon;
    }

    public boolean getIsCSS() {
        return this.isCSS;
    }

    public byte[] getRequestForTrace() {
        return this.requestForTrace;
    }

    private String SanitizePath(String path) {
        String[] parts = path.split("/");
        ArrayList<String> sanitizedParts = new ArrayList<>();
        for (String part : parts) {
            if (!part.equals("..") && !part.equals(".")) {
                if (part.contains(".")) {
                    int qmarkIndex = part.indexOf("?");
                    if (qmarkIndex != -1) {
                        String[] fileAndParams = part.split("\\?");
                        part = fileAndParams[0];
                        parseParameters(fileAndParams[1]);
                    }
                }
                sanitizedParts.add(part);
            }
        }
        return String.join("/", sanitizedParts).replace("%20", " ");
    }

    private void parseParameters(String body) {
        String[] params = body.split("&");
        for (String param : params) {
            String[] keyAndValue = param.split("=");
            if (keyAndValue.length == 2) {
                parameters.put(keyAndValue[0], keyAndValue[1]);
            }
        }
    }
}
