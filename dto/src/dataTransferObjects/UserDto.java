package dataTransferObjects;

import backend.User;

public class UserDto {
    public final String userName;
    public final String role;

    public UserDto(String userName, String role) {
        this.userName = userName;
        this.role = role;
    }

    public UserDto(User user) {
        this.userName = user.getUsername();
        this.role = user.getRole();
    }

    public final String getRole() {
        return role;
    }

    public final String getUserName() {
        return userName;
    }
}
