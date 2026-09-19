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

import service.StudentService;

@WebServlet("/PreviousResultsServlet")
public class PreviousResultsServlet extends HttpServlet {
    private static final long serialVersionUID=1L;

    private StudentService studentService;

    @Override
    public void init() throws ServletException {
        studentService=new StudentService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,IOException {

        HttpSession session=request.getSession(false);

        if(session==null ||
           session.getAttribute("userId")==null ||
           !"STUDENT".equals(session.getAttribute("role"))) {

            response.sendRedirect(
                    request.getContextPath()+"/login.jsp");

            return;
        }

        Object studentIdObject=
                session.getAttribute("studentId");

        if(studentIdObject==null) {
            response.sendRedirect(
                    request.getContextPath()+"/login.jsp");

            return;
        }

        int studentId;

        try {
            studentId=(Integer)studentIdObject;
        }catch(Exception e) {
            response.sendRedirect(
                    request.getContextPath()+"/login.jsp");

            return;
        }

        List<Integer> semesters=
                studentService.getPublishedSemesters(studentId);

        request.setAttribute("semesters",semesters);

        String semesterParameter=
                request.getParameter("semester");

        if(semesterParameter!=null &&
           !semesterParameter.trim().isEmpty()) {

            try {
                int semester=
                        Integer.parseInt(semesterParameter);

                Map<String,Object> semesterResult=
                        studentService.getSemesterResult(
                                studentId,
                                semester);

                List<Map<String,Object>> subjects=
                        studentService.getSemesterSubjects(
                                studentId,
                                semester);

                request.setAttribute(
                        "semesterResult",
                        semesterResult);

                request.setAttribute(
                        "semesterSubjects",
                        subjects);

                request.setAttribute(
                        "selectedSemester",
                        semester);

            }catch(NumberFormatException e) {
                request.setAttribute(
                        "errorMessage",
                        "Invalid semester selected.");
            }
        }

        request.getRequestDispatcher(
                "/previousResults.jsp")
                .forward(request,response);
    }
}