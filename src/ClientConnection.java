import java.io.*;
import java.net.Socket;

public class ClientConnection implements Runnable{
    private Socket clientSocket;
    private HTTPRequest request;
    private HTTPResponse response;

    ClientConnection(Socket clientSocket) {
        this.clientSocket = clientSocket;
//        try {
//            this.clientSocket.setSoTimeout(500);
//        } catch (SocketException e) {
//        }

    }

    @Override
    public void run() {
        String fullClientRequest;
        StringBuilder clientRequest = new StringBuilder();
        String line;
        try (
                BufferedReader inFromClient =
                        new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            line = inFromClient.readLine();
            while (line != null && !line.isEmpty()) {
                clientRequest.append(line).append("\n");
                line = inFromClient.readLine();
            }
            fullClientRequest = clientRequest.toString();
            System.out.println("HTTP Request: \n" + fullClientRequest + "\n");
            if (!fullClientRequest.isEmpty()) {
                request = new HTTPRequest(fullClientRequest);
                response = new HTTPResponse(request);
                outToClient.writeBytes("HTTP/1.1 200 OK\r\n"); // fixme: this is a placeholder
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