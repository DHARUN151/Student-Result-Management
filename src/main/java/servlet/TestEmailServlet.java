package servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import service.EmailService;

@WebServlet("/testEmail")
public class TestEmailServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        try {

            EmailService emailService = new EmailService();

            boolean sent = emailService.sendResultPublishedEmail(
                    "sdkkrr2006@gmail.com",
                    "Test Student",
                    1,
                    "http://localhost:8080/StudentResultManagement/viewResult"
            );

            if (sent) {
                response.getWriter().println(
                        "<h2>Email sent successfully!</h2>"
                );
            } else {
                response.getWriter().println(
                        "<h2>Email sending failed.</h2>"
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.getWriter().println(
                    "<h2>Email Error</h2>"
            );

            response.getWriter().println(
                    "<p>" + e.getMessage() + "</p>"
            );
        }
    }
}