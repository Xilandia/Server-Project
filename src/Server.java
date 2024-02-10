import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private Config config;

    public void StartServer() throws Exception{
        config = new Config("config.ini");

        ServerSocket welcomeSocket = new ServerSocket(config.getPort());  // bind + listen
        ExecutorService executor = Executors.newFixedThreadPool(config.getMaxThreads());

        while (true) {
            Socket clientSocket = welcomeSocket.accept();
            Runnable worker = new EchoRunnable(clientSocket);

            executor.execute(worker);
        }
        // executor.shutdown();
    }
}