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

import service.MissingMarksService;

@WebServlet("/MissingMarksServlet")
public class MissingMarksServlet extends HttpServlet {

    private static final long serialVersionUID=1L;

    private MissingMarksService service=
            new MissingMarksService();

    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,IOException {

        HttpSession session=
                request.getSession(false);

        if(session==null ||
           session.getAttribute("userId")==null) {

            response.sendRedirect(
                    request.getContextPath()+"/login.jsp");

            return;
        }

        int userId=
                (Integer)session.getAttribute("userId");

        String role=
                (String)session.getAttribute("role");

        if("FACULTY".equals(role)) {

            List<Map<String,Object>> missingMarks=
                    service.getFacultyMissingMarks(userId);

            request.setAttribute(
                    "missingMarks",
                    missingMarks);

        } else if("HOD".equals(role)) {

            List<Map<String,Object>> missingMarks=
                    service.getHODMissingMarks(userId);

            request.setAttribute(
                    "missingMarks",
                    missingMarks);

        } else {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Only Faculty and HOD users can access missing marks.");

            return;
        }

        request.setAttribute("userRole",role);

        request.getRequestDispatcher(
                "/missingMarks.jsp")
                .forward(request,response);
    }
}