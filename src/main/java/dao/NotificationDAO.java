package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.DBConnection;

public class NotificationDAO {

    public boolean createNotification(
            int studentId,
            int resultId,
            String title,
            String message,
            String resultLink) {

        String sql =
                "INSERT INTO enterprise.notifications " +
                "(student_id, result_id, title, message, result_link) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, studentId);
            statement.setInt(2, resultId);
            statement.setString(3, title);
            statement.setString(4, message);
            statement.setString(5, resultLink);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Error creating notification");
            e.printStackTrace();
        }

        return false;
    }

    public List<Map<String, Object>> getStudentNotifications(
            int studentId) {

        List<Map<String, Object>> notifications =
                new ArrayList<>();

        String sql =
                "SELECT notification_id, " +
                "title, message, result_link, " +
                "is_read, created_at " +
                "FROM enterprise.notifications " +
                "WHERE student_id=? " +
                "ORDER BY created_at DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, studentId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    Map<String, Object> notification =
                            new HashMap<>();

                    notification.put(
                            "notificationId",
                            result.getInt("notification_id"));

                    notification.put(
                            "title",
                            result.getString("title"));

                    notification.put(
                            "message",
                            result.getString("message"));

                    notification.put(
                            "resultLink",
                            result.getString("result_link"));

                    notification.put(
                            "isRead",
                            result.getBoolean("is_read"));

                    notification.put(
                            "createdAt",
                            result.getTimestamp("created_at"));

                    notifications.add(notification);
                }
            }

        } catch (Exception e) {
            System.out.println(
                    "Error getting student notifications");
            e.printStackTrace();
        }

        return notifications;
    }

    public int getUnreadCount(int studentId) {

        String sql =
                "SELECT COUNT(*) " +
                "FROM enterprise.notifications " +
                "WHERE student_id=? " +
                "AND is_read=false";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, studentId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return result.getInt(1);
                }
            }

        } catch (Exception e) {
            System.out.println(
                    "Error getting unread notification count");
            e.printStackTrace();
        }

        return 0;
    }

    public boolean markAsRead(
            int notificationId,
            int studentId) {

        String sql =
                "UPDATE enterprise.notifications " +
                "SET is_read=true " +
                "WHERE notification_id=? " +
                "AND student_id=?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, notificationId);
            statement.setInt(2, studentId);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println(
                    "Error marking notification as read");
            e.printStackTrace();
        }

        return false;
    }

    public boolean markEmailSent(
            int notificationId) {

        String sql =
                "UPDATE enterprise.notifications " +
                "SET email_sent=true, " +
                "email_sent_at=CURRENT_TIMESTAMP " +
                "WHERE notification_id=?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, notificationId);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println(
                    "Error updating email status");
            e.printStackTrace();
        }

        return false;
    }

    public int getLatestNotificationId(
            int studentId,
            int resultId) {

        String sql =
                "SELECT notification_id " +
                "FROM enterprise.notifications " +
                "WHERE student_id=? " +
                "AND result_id=? " +
                "ORDER BY notification_id DESC " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, studentId);
            statement.setInt(2, resultId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return result.getInt("notification_id");
                }
            }

        } catch (Exception e) {
            System.out.println(
                    "Error getting notification ID");
            e.printStackTrace();
        }

        return 0;
    }
}