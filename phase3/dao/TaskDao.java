package phase3.dao;

import phase3.model.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDao {
    
    private final String url;
    private final String user;
    private final String pass;

    public TaskDao(String url, String user, String pass) {
        this.url = url;
        this.user = user;
        this.pass = pass;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url,user,pass);
    }

    public long insert(String title) throws SQLException {
        String sql = "INSERT INTO tasks(title) VALUES (?)";

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1,title);
            ps.executeUpdate();

            try(ResultSet keys = ps.getGeneratedKeys()) {
                if(keys.next()) {
                    return keys.getLong(1);
                }
                throw new SQLException("No generated key returned");
            }
        }
    }

    public List<Task> findAll() throws SQLException {
        String sql = "SELECT id, title, created_at FROM tasks ORDER BY id";
        List<Task> list = new ArrayList<>();

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while(rs.next()) {
                long id = rs.getLong("id");
                String title = rs.getString("title");
                Timestamp createAt = rs.getTimestamp("created_at");
                list.add(new Task(id, title, String.valueOf(createAt)));
            }
        }
        return list;
    }
}
