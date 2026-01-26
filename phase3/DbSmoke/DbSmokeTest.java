package phase3.DbSmoke;

import java.sql.*;

public class DbSmokeTest {
    public static void main(String[] args) throws Exception {
        String url =
            "jdbc:mysql://localhost:3306/appdb" +
            "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Tokyo";

        String user = "root";
        String pass = "rootpass";

        try (Connection con = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = con.prepareStatement(
                 "SELECT id, title, created_at FROM tasks ORDER BY id"
             );
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                long id = rs.getLong("id");
                String title = rs.getString("title");
                Timestamp createdAt = rs.getTimestamp("created_at");
                System.out.println(id + " " + title + " " + createdAt);
            }
        }
    }
}
