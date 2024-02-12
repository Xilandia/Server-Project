import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

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
        int contentLength = 0;
        try (
                BufferedReader inFromClient =
                        new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            line = inFromClient.readLine();
            while (line != null && !line.isEmpty()) {
                clientRequest.append(line).append("\n");
                if (line.startsWith("Content-Length: ")) {
                    contentLength = Integer.parseInt(line.substring("Content-Length: ".length()));
                }
                line = inFromClient.readLine();
            }
            fullClientRequest = clientRequest.toString();
            System.out.println("Request header: \n" + fullClientRequest + "\n");
            if (contentLength > 0) {
                char[] body = new char[contentLength];
                inFromClient.read(body, 0, contentLength);
                fullClientRequest += new String(body);
            }
            if (!fullClientRequest.isEmpty()) {
                request = new HTTPRequest(fullClientRequest);
                response = new HTTPResponse(request);
                System.out.println("Response header:\n" + response.buildHeaders() + "\n");
                outToClient.writeBytes(response.buildHeaders());
                if (response.getSendDecision()) {
                    Files.copy(response.getFile().toPath(), outToClient);
                } else if (request.getMethod() == HTTPRequest.Method.TRACE) {
                    outToClient.write(request.getRequestForTrace());
                }
                outToClient.flush();
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