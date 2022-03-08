package dataTransferObjects;

public class UserDto {
    public final String userName;
    public final String role;

    public UserDto(String userName, String role) {
        this.userName = userName;
        this.role = role;
    }

    public final String getRole() {
        return role;
    }

    public final String getUserName() {
        return userName;
    }
}
