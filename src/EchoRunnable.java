import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class EchoRunnable implements Runnable{
    private Socket clientSocket = null;

    EchoRunnable(Socket clientSocket) {
        this.clientSocket = clientSocket;
//        try {
//            this.clientSocket.setSoTimeout(500);
//        } catch (SocketException e) {
//        }

    }

    @Override
    public void run() {
        String clientSentence;
        String capitalizedSentence;
        try (
                BufferedReader inFromClient =
                        new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            clientSentence = inFromClient.readLine();  //read
            System.out.println("Received: " + clientSentence);
            if (clientSentence != null) {
                capitalizedSentence = clientSentence.toUpperCase() + '\n';
                outToClient.writeBytes(capitalizedSentence);  // write
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                this.clientSocket.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}