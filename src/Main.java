public class Main {
    public static void main(String[] args) {
        Server server = new Server();
        try {
            server.StartServer();
        } catch (Exception e) {
            System.err.println("Server failed to start");
            System.err.println(e.getMessage());
        }
    }
}