package servlet;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import util.DBConnection;
@WebServlet("/viewResult")
public class ResultServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !"STUDENT".equals(session.getAttribute("role"))) {
            response.sendRedirect("login.jsp");
            return;
        }
        try {
            int studentId = (Integer) session.getAttribute("studentId");
            Connection connection = DBConnection.getConnection();
            String sql = "SELECT st.reg_num, st.name, st.dob, st.gender, p.program_name, d.department_name, ay.year_name, se.semester_number, su.subject_code, su.subject_name, su.credits, m.internal_mark, m.external_mark, m.total_mark, m.grade, m.result_outcome, r.total_marks, r.maximum_marks, r.percentage, r.cgpa, r.result_class, r.workflow_status FROM enterprise.result r JOIN enterprise.students st ON r.student_id = st.student_id JOIN enterprise.academic_details ad ON ad.student_id = st.student_id AND ad.current_semester_id = r.semester_id JOIN enterprise.programs p ON ad.program_id = p.program_id JOIN enterprise.departments d ON p.department_id = d.department_id JOIN enterprise.semesters se ON r.semester_id = se.semester_id JOIN enterprise.academic_years ay ON se.academic_year_id = ay.academic_year_id JOIN enterprise.marks m ON m.student_id = st.student_id AND m.exam_id = r.exam_id JOIN enterprise.subject_offerings so ON m.offering_id = so.offering_id AND so.semester_id = r.semester_id JOIN enterprise.subjects su ON so.subject_id = su.subject_id WHERE r.student_id = ? AND r.workflow_status = 'PUBLISHED' ORDER BY su.subject_code";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, studentId);
            ResultSet result = statement.executeQuery();
            request.setAttribute("result", result);
            request.getRequestDispatcher("result.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("text/html");
            response.getWriter().println("<h2>Error while getting result</h2>");
            response.getWriter().println("<p>" + e.getMessage() + "</p>");
        }
    }
}