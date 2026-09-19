package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import util.DBConnection;

public class MarksheetVerificationDAO {

    public boolean createVerification(
            int resultId,
            String verificationCode) {

        String sql =
                "INSERT INTO enterprise.marksheet_verification " +
                "(result_id, verification_code) " +
                "VALUES (?, ?)";

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    resultId);

            statement.setString(
                    2,
                    verificationCode);

            return statement.executeUpdate() == 1;

        } catch (Exception e) {

            System.out.println(
                    "Unable to create marksheet verification");

            System.out.println(
                    e.getMessage());

            return false;
        }
    }

    public String getVerificationCode(
            int resultId) {

        String sql =
                "SELECT verification_code " +
                "FROM enterprise.marksheet_verification " +
                "WHERE result_id = ?";

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    resultId);

            try (
                    ResultSet result =
                            statement.executeQuery()
            ) {

                if (result.next()) {

                    return result.getString(
                            "verification_code");
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "Unable to get verification code");

            System.out.println(
                    e.getMessage());
        }

        return null;
    }

    public boolean verificationCodeExists(
            String verificationCode) {

        String sql =
                "SELECT verification_id " +
                "FROM enterprise.marksheet_verification " +
                "WHERE verification_code = ?";

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    verificationCode);

            try (
                    ResultSet result =
                            statement.executeQuery()
            ) {

                return result.next();
            }

        } catch (Exception e) {

            System.out.println(
                    "Unable to check verification code");

            System.out.println(
                    e.getMessage());

            return false;
        }
    }

    public boolean verifyPublishedMarksheet(
            String verificationCode) {

        String sql =
                "SELECT mv.verification_id " +
                "FROM enterprise.marksheet_verification mv " +
                "JOIN enterprise.result r " +
                "ON mv.result_id = r.result_id " +
                "WHERE mv.verification_code = ? " +
                "AND r.workflow_status = 'PUBLISHED'";

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    verificationCode);

            try (
                    ResultSet result =
                            statement.executeQuery()
            ) {

                return result.next();
            }

        } catch (Exception e) {

            System.out.println(
                    "Unable to verify marksheet");

            System.out.println(
                    e.getMessage());

            return false;
        }
    }
}