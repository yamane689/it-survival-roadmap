package phase3.DbSmoke;

import phase3.dao.TaskDao;
import phase3.model.Task;

import java.util.List;

public class DbSmokeTest {
    public static void main(String[] args) throws Exception {
        String url =
            "jdbc:mysql://localhost:3306/appdb" +
            "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Tokyo";
        String user = "root";
        String pass = "rootpass";

        TaskDao dao = new TaskDao(url, user, pass);

        long newId = dao.insert("dao inserted task");
        System.out.println("inserted id =" + newId);

        List<Task> tasks = dao.findAll();
        for(Task t : tasks){
            System.out.println(t.id + " " + t.title + " " + t.createdAt);
        }
    }
}
