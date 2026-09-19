package servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import service.MarksheetVerificationService;

@WebServlet("/verifyMarksheet")
public class VerifyMarksheetServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final MarksheetVerificationService service =
            new MarksheetVerificationService();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String verificationCode =
                request.getParameter("code");

        boolean verified = false;

        if (verificationCode != null &&
                !verificationCode.trim().isEmpty()) {

            verified =
                    service.verifyMarksheet(
                            verificationCode);
        }

        request.setAttribute(
                "verificationCode",
                verificationCode);

        request.setAttribute(
                "verified",
                verified);

        request.getRequestDispatcher(
                "/verifyMarksheet.jsp")
                .forward(
                        request,
                        response);
    }
}