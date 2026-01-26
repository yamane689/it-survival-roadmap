package phase2.SocketServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import phase3.dao.TaskDao;

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
        System.out.println("requestLine: " + requestLine);

        int contentLength = 0;

        // リクエストの読み込み
        String line;
        String contentType = "";
        // HTTPリクエストを1行ずつ読み込む
        while ((line = in.readLine()) != null) {
            // 空行がHTTPヘッダの終わりを表す
            if (line.isEmpty())
                break;
            System.out.println(line);

            // HTTPヘッダの中からContent-Length(リクエストボディのバイト数)を探す
            if (line.toLowerCase().startsWith("content-length:")) {
                // 数値部分だけの切り出し
                String value = line.substring("content-length:".length()).trim();
                // String→int変換
                contentLength = Integer.parseInt(value);
            }

            if (line.toLowerCase().startsWith("content-type:")) {
                contentType = line.substring("content-type:".length()).trim();
            }

        }
        System.out.println("----- request end -----");

        // Content-Length で指定されたサイズ分のリクエストボディを格納する配列を用意
        char[] bodyChars = new char[contentLength];
        // 書き込み開始位置
        int readTotal = 0;
        while (readTotal < contentLength) {
            int n = in.read(bodyChars, readTotal, contentLength - readTotal);
            if (n == -1) 
                break;
            readTotal += n;
        }
        // リクエストボディをString型に変換
        String requestBody = new String(bodyChars, 0, readTotal);

        if (contentType.startsWith("application/json")) {
            System.out.println("----- json body -----");
            System.out.println(requestBody);
        }

        System.out.println("----- body start -----");
        System.out.println(requestBody);
        // リクエストボディをURLデコード,Mapに変換
        var params = parseFormUrlEncoded(requestBody);
        System.out.println("----- body end -----");

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

        String method = "GET";
        if (requestLine != null) {
            String[] parts = requestLine.split(" ");
            if (parts.length >= 1) {
                method = parts[0];
            }
        }

        String statusLine = "HTTP/1.1 200 OK\r\n";
        String body;

        String url =
            "jdbc:mysql://localhost:3306/appdb" +
            "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Tokyo";
        String user = "root";
        String pass = "rootpass";

        TaskDao dao = new TaskDao(url, user, pass);

        if (method.equals("POST") && path.equals("/tasks")) {
            String title = params.get("title");
            long id = dao.insert(title);

            body = "created id=" + id;
        }
        else if (contentType.startsWith("application/json")) {
            var jsonMap = parseSimpleJsonObject(requestBody);
            System.out.println("parsed json = " + jsonMap);

            body = "JSON parsed:" + jsonMap + "\n";
        }
        else if("POST".equals(method)){
            body = "You posted: " + params + "\n";
        } else if ("/hello".equals(path)) {
            statusLine = "HTTP/1.1 200 OK\r\n";
            body = "Hello route!\n";
        } else {
            statusLine = "HTTP/1.1 404 Not Found\r\n";
            body = "404 Not Found\n";
        }

        // HTTPレスポンスを手動で組み立て(ヘッダ+ボディ)
        String response = 
            statusLine +
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

    // リクエストボディをMapに変換する
    private static java.util.Map<String, String> parseFormUrlEncoded(String body) {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        // bodyがない場合は終了
        if (body == null || body.isEmpty()) 
            return map;
        // & で分割した配列を拡張For文でループ
        for (String pair : body.split("&")) {
            // KeyとValueに分割
            String[] kv = pair.split("=", 2);
            // KeyのURLデコード
            String key = java.net.URLDecoder.decode(kv[0], java.nio.charset.StandardCharsets.UTF_8);
            // ValueのURLデコード
            String value = kv.length > 1
                    ? java.net.URLDecoder.decode(kv[1], java.nio.charset.StandardCharsets.UTF_8)
                    : "";
            map.put(key, value);
        }
        return map;
    }

    private static java.util.Map<String, String> parseSimpleJsonObject(String json) {
        java.util.Map<String, String> map = new java.util.HashMap<>();

        if (json == null) {
            return map;
        }

        // 前後の空白を除去
        String s = json.trim();
        // { } を外す
        if (s.startsWith("{")) s = s.substring(1);
        if (s.endsWith("}")) s = s.substring(0, s.length() - 1);

        s = s.trim();
        if (s.isEmpty()) {
            return map;
        }

        // カンマで分割
        for (String pair : s.split(",")) {
            // KeyとValueの分離
            String[] kv = pair.split(":", 2);
            // ：がないデータをスキップ
            if (kv.length < 2) continue;

            String key = kv[0].trim();
            String value = kv[1].trim();

            // ダブルクォートを外す（"name" → name）
            key = stripQuotes(key);
            value = stripQuotes(value);

            map.put(key, value);
        }
        return map;
    }

    private static String stripQuotes(String s) {
        if (s == null) {
            return "";
        }

        s = s.trim();

        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }
}

