import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public void StartServer(int port) throws Exception{
        ServerSocket welcomeSocket = new ServerSocket(port);  // bind + listen
        ExecutorService executor = Executors.newFixedThreadPool(10);

        while (true) {
            Socket clientSocket = welcomeSocket.accept();
            Runnable worker = new EchoRunnable(clientSocket);

            executor.execute(worker);
        }
        // executor.shutdown();
    }
}