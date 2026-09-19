package servlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import service.NotificationService;

@WebServlet("/notifications")
public class NotificationServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final NotificationService notificationService =
            new NotificationService();

    @Override
    protected void doGet(
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

        int studentId;

        try {

            studentId =
                    Integer.parseInt(
                            studentIdObject.toString());

        } catch (Exception e) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/login.jsp");

            return;
        }

        List<Map<String, Object>> notifications =
                notificationService.getStudentNotifications(
                        studentId);

        int unreadCount =
                notificationService.getUnreadCount(
                        studentId);

        request.setAttribute(
                "notifications",
                notifications);

        request.setAttribute(
                "unreadCount",
                unreadCount);

        request.getRequestDispatcher(
                "notifications.jsp")
                .forward(
                        request,
                        response);
    }
}