package util;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.mindrot.jbcrypt.BCrypt;

public class UserInitializer {

    public static void main(String[] args) {

        try {

            // Teacher login details
            String username = "teacher1";
            String password = "teacher123";
            String role = "TEACHER";

            // Hash the password
            String hashedPassword =
                    BCrypt.hashpw(password, BCrypt.gensalt());
            // Connect to database
            Connection connection =
                    DBConnection.getConnection();
            // Insert teacher
            String sql =
                    "INSERT INTO users " +
                    "(username, password, role) " +
                    "VALUES (?, ?, ?)";

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setString(1, username);
            statement.setString(2, hashedPassword);
            statement.setString(3, role);

            statement.executeUpdate();

            System.out.println("Teacher account created successfully!");
            System.out.println("Username: " + username);

            connection.close();

        } catch (Exception e) {

            System.out.println(e);
        }
    }
}