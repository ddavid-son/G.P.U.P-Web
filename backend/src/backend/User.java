package backend;

public class User {
    public String username;
    public String role;

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