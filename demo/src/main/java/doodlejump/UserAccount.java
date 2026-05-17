package doodlejump;

public class UserAccount extends Account {
    private String email;
    private String role;

    public UserAccount(String username, String password, String email, String role) {
        super(username, password, AccountType.USER);
        this.email = email;
        this.role = role;
    }

    public UserAccount(String username, String passwordHash, String email, String role, boolean isHash) {
        super(username, passwordHash, AccountType.USER, true);
        this.email = email;
        this.role = role == null || role.isBlank() ? "user" : role;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String serialize() {
        return String.format("%s|%s|%s|%s|%s", type, username, passwordHash, email, role);
    }
}
