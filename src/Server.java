import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static Config config;
    private int port;

    public void StartServer() throws Exception {
        config = new Config("config.ini");
        port = config.getPort();
        System.out.println("Listening on port " + port);

        ServerSocket welcomeSocket = new ServerSocket(port);
        ExecutorService executor = Executors.newFixedThreadPool(config.getMaxThreads());

        while (true) {
            Socket clientSocket = welcomeSocket.accept();
            Runnable worker = new ClientConnection(clientSocket);

            executor.execute(worker);
        }
    }
}