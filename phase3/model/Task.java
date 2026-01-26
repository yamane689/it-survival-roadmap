package phase3.model;

public class Task {
    public final long id;
    public final String title;
    public final String createdAt;

    public Task(long id, String title, String createdAt) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
    }
}
