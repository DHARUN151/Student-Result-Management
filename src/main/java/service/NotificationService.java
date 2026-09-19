package service;

import java.util.List;
import java.util.Map;

import dao.NotificationDAO;

public class NotificationService {

    private final NotificationDAO notificationDAO;
    private final EmailService emailService;

    public NotificationService() {
        this.notificationDAO = new NotificationDAO();
        this.emailService = new EmailService();
    }

    public List<Map<String,Object>> getStudentNotifications(
            int studentId) {

        try {
            return notificationDAO.getStudentNotifications(
                    studentId);
        } catch(Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public int getUnreadCount(int studentId) {

        try {
            return notificationDAO.getUnreadCount(
                    studentId);
        } catch(Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean markAsRead(
            int notificationId,
            int studentId) {

        try {
            return notificationDAO.markAsRead(
                    notificationId,
                    studentId);
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean notifyResultPublished(
            int studentId,
            int resultId,
            String studentEmail,
            String studentName,
            int semester,
            String resultLink) {

        try {

            String title =
                    "Result Published";

            String message =
                    "Dear " + studentName
                    + ", your Semester "
                    + semester
                    + " result has been published. "
                    + "You can view your result using "
                    + "the link provided.";

            boolean notificationCreated =
                    notificationDAO.createNotification(
                            studentId,
                            resultId,
                            title,
                            message,
                            resultLink);

            if(!notificationCreated) {

                System.out.println(
                        "Failed to create in-app notification.");

                return false;
            }

            if(studentEmail != null &&
               !studentEmail.trim().isEmpty()) {

                boolean emailSent =
                        emailService.sendResultPublishedEmail(
                                studentEmail,
                                studentName,
                                semester,
                                resultLink);

                if(emailSent) {

                    int notificationId =
                            notificationDAO
                            .getLatestNotificationId(
                                    studentId,
                                    resultId);

                    if(notificationId>0) {

                        notificationDAO.markEmailSent(
                                notificationId);
                    }

                    System.out.println(
                            "Result email sent to: "
                            + studentEmail);

                } else {

                    System.out.println(
                            "Result email could not be sent.");
                }
            }

            return true;

        } catch(Exception e) {

            e.printStackTrace();

            return false;
        }
    }
}