public class Main {
    public static void main(String[] args) {
        Server server = new Server();
        try {
            server.StartServer(6789);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}