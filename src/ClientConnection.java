import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

public class ClientConnection implements Runnable{
    private Socket clientSocket;
    private HTTPRequest request;
    private HTTPResponse response;

    ClientConnection(Socket clientSocket) {
        this.clientSocket = clientSocket;
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
                fullClientRequest += "\r\n\r\n" + new String(body);
            }
            if (!fullClientRequest.isEmpty()) {
                request = new HTTPRequest(fullClientRequest);
                response = new HTTPResponse(request);
                if (request.getIsChunked()) {
                    System.out.println("Response header:\n" + response.buildChunkedHeaders() + "\n");
                    outToClient.writeBytes(response.buildChunkedHeaders());
                    if (response.getSendDecision()) {
                        sendChunkedFile(outToClient);
                    } else if (request.getMethod() == HTTPRequest.Method.TRACE) {
                        sendChunkedTrace(outToClient);
                    }
                    outToClient.flush();
                } else {
                    System.out.println("Response header:\n" + response.buildHeaders() + "\n");
                    outToClient.writeBytes(response.buildHeaders());
                    if (response.getParamsInfo()) {
                        outToClient.write(response.getParamsInfoResponse());
                    } else if (response.getSendDecision()) {
                        Files.copy(response.getFile().toPath(), outToClient);
                    } else if (request.getMethod() == HTTPRequest.Method.TRACE) {
                        outToClient.write(request.getRequestForTrace());
                    }
                    outToClient.flush();
                }
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

    private void sendChunkedFile(DataOutputStream outToClient) {
        int offset = 0;
        int chunkSize;
        int chunkLength = Server.config.getChunkLength();
        try {
            byte[] file = Files.readAllBytes(response.getFile().toPath());

            while (offset < response.getContentLength()) {
                chunkSize = Math.min(chunkLength, response.getContentLength() - offset);
                outToClient.write((Integer.toHexString(chunkSize) + "\r\n").getBytes());
                outToClient.write(file, offset, chunkSize);
                outToClient.write("\r\n".getBytes());
                offset += chunkLength;
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void sendChunkedTrace(DataOutputStream outToClient) {
        int offset = 0;
        int chunkSize;
        int chunkLength = Server.config.getChunkLength();
        try {
            byte[] trace = request.getRequestForTrace();

            while (offset < response.getContentLength()) {
                chunkSize = Math.min(chunkLength, response.getContentLength() - offset);
                outToClient.write((Integer.toHexString(chunkSize) + "\r\n").getBytes());
                outToClient.write(trace, offset, chunkSize);
                outToClient.write("\r\n".getBytes());
                offset += chunkLength;
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}