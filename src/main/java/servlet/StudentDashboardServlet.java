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

@WebServlet("/StudentDashboardServlet")
public class StudentDashboardServlet extends HttpServlet {
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

        if(session==null||
           session.getAttribute("userId")==null||
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

        Map<String,Object> student=
                studentService.getStudentDashboard(studentId);

        if(student==null||student.isEmpty()) {
            request.setAttribute(
                    "errorMessage",
                    "Student information could not be loaded.");

            request.getRequestDispatcher(
                    "/studentDashboard.jsp")
                    .forward(request,response);

            return;
        }

        List<Map<String,Object>> previousResults=
                studentService.getPreviousResults(studentId);

        double overallCgpa=
                studentService.getOverallCgpa(studentId);

        request.setAttribute("student",student);
        request.setAttribute("previousResults",previousResults);
        request.setAttribute("overallCgpa",overallCgpa);

        request.getRequestDispatcher(
                "/studentDashboard.jsp")
                .forward(request,response);
    }
}