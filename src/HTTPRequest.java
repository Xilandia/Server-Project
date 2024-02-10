import java.util.HashMap;

public class HTTPRequest {
    public enum Method {
        GET, POST
    }

    private Method method;
    private String path;
    private boolean isImage;
    private boolean isIcon;
    private String referer;
    private String userAgent;
    private HashMap<String, String> parameters;
    public HTTPRequest(String request) {
        parseRequest(request);
    }

    private void parseRequest(String request) {
        String[] lines = request.split("\n");
        String[] firstLine = lines[0].split(" ");
        String method = firstLine[0];
        String path = firstLine[1];
        this.method = Method.valueOf(method);
        this.path = path;
        this.isImage = path.endsWith(".bmp") || path.endsWith(".gif") || path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg"); // bonus - want to support jpeg bc why not
        this.isIcon = path.endsWith(".ico");
        this.parameters = new HashMap<String, String>();
        for (int i = 1; i < lines.length; i++) {
            String[] parts = lines[i].split(": ");
            if (parts[0].equals("Referer")) {
                this.referer = parts[1];
            } else if (parts[0].equals("User-Agent")) {
                this.userAgent = parts[1];
            }
        }

        System.out.println("Method: " + this.method);
        System.out.println("Path: " + this.path);
        System.out.println("Is Image: " + this.isImage);
        System.out.println("Is Icon: " + this.isIcon);
        System.out.println("Referer: " + this.referer);
        System.out.println("User-Agent: " + this.userAgent);
    }
}