import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class HTTPRequest {
    public enum Method {
        GET, POST, HEAD, TRACE, PUT, DELETE, CONNECT, OPTIONS, PATCH
    }

    private Method method;
    private String path;
    private boolean isHTML;
    private boolean isImage;
    private boolean isIcon;
    private boolean isCSS;
    private String referer;
    private String userAgent;
    private boolean isChunked;
    private byte[] requestForTrace;
    private static HashMap<String, String> parameters = new HashMap<>();

    public HTTPRequest(String request) {
        String[] lines = request.split("\n");
        String[] firstLine = lines[0].split(" ");
        String method = firstLine[0];
        String path = SanitizePath(firstLine[1]);
        this.method = Method.valueOf(method);
        this.path = path;
        this.isHTML = path.endsWith(".html") || path.endsWith(".htm") || path.equals("/");
        this.isImage = path.endsWith(".bmp") || path.endsWith(".gif") || path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg");
        this.isIcon = path.endsWith(".ico");
        this.isCSS = path.endsWith(".css");
        for (int i = 1; i < lines.length; i++) {
            String[] parts = lines[i].split(": ");
            parts[0] = parts[0].toLowerCase();
            switch (parts[0]) {
                case "referer" -> this.referer = parts[1];
                case "user-agent" -> this.userAgent = parts[1];
                case "chunked" -> this.isChunked = parts[1].equalsIgnoreCase("yes");
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

    public boolean getIsChunked() {
        return this.isChunked;
    }

    public HashMap<String, String> getParameters() {
        return parameters;
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
                if (keyAndValue[0].equals("params")) {
                    String[] decodedParams = URLDecoder.decode(keyAndValue[1], StandardCharsets.UTF_8).split("\n");
                    for (String decodedParam : decodedParams) {
                        String[] decodedKeyAndValue = decodedParam.split("=");
                        if (decodedKeyAndValue.length == 2) {
                            parameters.put(decodedKeyAndValue[0], decodedKeyAndValue[1]);
                        }
                    }
                } else {
                    parameters.put(keyAndValue[0], keyAndValue[1]);
                }
            }
        }
    }
}
