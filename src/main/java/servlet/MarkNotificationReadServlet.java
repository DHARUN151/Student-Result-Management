package servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import service.NotificationService;

@WebServlet("/markNotificationRead")
public class MarkNotificationReadServlet
        extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final NotificationService notificationService =
            new NotificationService();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);

        if (session == null ||
                !"STUDENT".equals(
                        session.getAttribute("role"))) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/login.jsp");

            return;
        }

        Object studentIdObject =
                session.getAttribute("studentId");

        if (studentIdObject == null) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/login.jsp");

            return;
        }

        int studentId =
                Integer.parseInt(
                        studentIdObject.toString());

        String notificationParameter =
                request.getParameter(
                        "notificationId");

        try {

            int notificationId =
                    Integer.parseInt(
                            notificationParameter);

            notificationService.markAsRead(
                    notificationId,
                    studentId);

        } catch (Exception e) {

            System.out.println(
                    "Invalid notification ID.");
        }

        response.sendRedirect(
                request.getContextPath()
                        + "/notifications");
    }
}