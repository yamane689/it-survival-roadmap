package phase2.SocketServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws Exception {
        int port = 8080;

        System.out.println("Starting server on port " + port);

        ServerSocket serverSocket = new ServerSocket(port);
        Socket socket = serverSocket.accept();
        System.out.println("Client connected");

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String line;
        System.out.println("----- request start -----");
        while ((line = in.readLine()) != null) {
            if (line.isEmpty()) break; // HTTPヘッダの終わり
            System.out.println(line);
        }
        System.out.println("----- request end -----");

        OutputStream out = socket.getOutputStream();

        String body = "Hello from SocketServer\n";
        String response = 
            "HTTP/1.1 200 OK\r\n" +
            "Content-Type: text/plain; charset=utf-8\r\n" +
            "Content-Length: " + body.getBytes("UTF-8").length + "\r\n" +
            "Connection: close\r\n" +
            "\r\n" +
            body;

        out.write(response.getBytes("UTF-8"));
        out.flush();

        socket.close();
        serverSocket.close();
    }
}
