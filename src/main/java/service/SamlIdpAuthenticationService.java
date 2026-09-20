package service;

import dao.UserDAO;
import model.User;

import org.mindrot.jbcrypt.BCrypt;

public class SamlIdpAuthenticationService {

    private final UserDAO userDAO =
            new UserDAO();

    public AuthenticationResult authenticate(
            String username,
            String password) {

        if (username == null
                || password == null
                || username.trim().isEmpty()
                || password.isEmpty()) {

            return AuthenticationResult.INVALID_CREDENTIALS;
        }

        username =
                username.trim();

        if (userDAO.isAccountLocked(username)) {

            return AuthenticationResult.ACCOUNT_LOCKED;
        }

        User user =
                userDAO.findUserByUsername(
                        username
                );

        if (user == null) {

            return AuthenticationResult.INVALID_CREDENTIALS;
        }

        if (!"ACTIVE".equals(
                user.getAccountStatus())) {

            return AuthenticationResult.INVALID_CREDENTIALS;
        }

        if (!isPrivilegedRole(
                user.getRole())) {

            return AuthenticationResult.ROLE_NOT_ALLOWED;
        }

        boolean passwordValid;

        try {

            passwordValid =
                    BCrypt.checkpw(
                            password,
                            user.getPasswordHash()
                    );

        } catch (Exception e) {

            passwordValid = false;
        }

        if (!passwordValid) {

            userDAO.recordFailedLogin(
                    username
            );

            return AuthenticationResult.INVALID_CREDENTIALS;
        }

        userDAO.clearLoginAttempts(
                username
        );

        if (!user.isTwoFactorEnabled()
                || user.getTwoFactorSecret() == null
                || user.getTwoFactorSecret().trim().isEmpty()) {

            return AuthenticationResult.TWO_FACTOR_NOT_CONFIGURED;
        }

        return AuthenticationResult.SUCCESS;
    }

    public User getUser(
            String username) {

        if (username == null
                || username.trim().isEmpty()) {

            return null;
        }

        return userDAO.findUserByUsername(
                username.trim()
        );
    }

    public boolean isPrivilegedRole(
            String role) {

        return "ADMIN".equals(role)
                || "SUPER_ADMIN".equals(role)
                || "FACULTY".equals(role)
                || "HOD".equals(role)
                || "EXAM_CELL".equals(role);
    }

    public enum AuthenticationResult {

        SUCCESS,

        INVALID_CREDENTIALS,

        ACCOUNT_LOCKED,

        ROLE_NOT_ALLOWED,

        TWO_FACTOR_NOT_CONFIGURED
    }
}