package backend;

import java.util.ArrayList;
import java.util.List;

public class User {
    public String username;
    public String role;
    public List<String> tasksImIn = new ArrayList<>();

    public List<String> getTasksImIn() {

        return tasksImIn;
    }

    public void addTask(String taskName) {

        tasksImIn.add(taskName);
    }

    public void removeTask(String taskName) {

        tasksImIn.remove(taskName);
    }

    public User(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public final String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public final String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}