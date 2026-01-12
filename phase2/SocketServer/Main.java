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

        // 8080番ポートを開く（LISTEN開始）
        ServerSocket serverSocket = new ServerSocket(port);
        // クライアントからの接続を待つ（ブロッキング）
        Socket socket = serverSocket.accept();
        System.out.println("Client connected");

        // クライアントが送ってきたデータを読むための入力ストリーム
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        // HTTPリクエストの最初の1行を読む
        String requestLine = in.readLine(); 
        System.out.println("----- request start -----");
        System.out.println(requestLine);

        String line;
        // HTTPリクエストを1行ずつ読み込む
        while ((line = in.readLine()) != null) {
            // 空行がHTTPヘッダの終わりを表す
            if (line.isEmpty())
                break;
            System.out.println(line);
        }
        System.out.println("----- request end -----");

        // リクエストされたパスを格納する変数
        String path = "/";
        // reqestLineが取得できている場合のみ解析する
        if (requestLine != null) {
            // reqestLineをスペースで分割する
            String[] parts = requestLine.split(" ");
            if (parts.length >= 2) {
                // 配列の2番目の要素がURLパス
                path = parts[1];
            }
        }

        // クライアントへデータを書き込むための出力ストリーム
        OutputStream out = socket.getOutputStream();

        // pathの値によってレスポンス内容を切り替える
        String body;
        if ("/hello".equals(path)) {
            body = "Hello route!\n";
        } else {
            body = "Root route. Try /hello\n";
        }

        // HTTPレスポンスを手動で組み立て(ヘッダ+ボディ)
        String response = 
            "HTTP/1.1 200 OK\r\n" +
            "Content-Type: text/plain; charset=utf-8\r\n" +
            "Content-Length: " + body.getBytes("UTF-8").length + "\r\n" +
            "Connection: close\r\n" +
            "\r\n" +
            body;

        // クライアントへレスポンスを送信
        out.write(response.getBytes("UTF-8"));
        out.flush();

        // クライアントとの接続をクローズ
        socket.close();
        // サーバを終了
        serverSocket.close();
    }
}
