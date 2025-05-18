import java.sql.*;
import java.util.Optional;

public class UserDAO {
    // Check if email already exists
    public boolean emailExists(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE Email = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Register a new user
    public boolean registerUser(String username, String email, String password) throws SQLException {
        if (emailExists(email)) {
            return false; // Email already exists
        }

        String query = "INSERT INTO users (Username, Email, Password) VALUES (?, ?, ?)";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, password);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Authenticate user
    public Optional<User> loginUser(String email, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE Email = ? AND Password = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(
                            rs.getInt("UserID"),
                            rs.getString("Username"),
                            rs.getString("Email"),
                            rs.getString("Password")
                    ));
                }
            }
        }
        return Optional.empty();
    }
}