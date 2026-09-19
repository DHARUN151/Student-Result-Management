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

import model.Marks;
import service.FacultySubjectService;
import service.MarksService;

@WebServlet("/AddMarksServlet")
public class AddMarksServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || !"FACULTY".equals(session.getAttribute("role"))) {
            response.sendRedirect("login.jsp");
            return;
        }

        Object userIdObject = session.getAttribute("userId");

        if (!(userIdObject instanceof Integer)) {
            response.sendRedirect("login.jsp");
            return;
        }

        int userId = (Integer) userIdObject;

        FacultySubjectService service = new FacultySubjectService();

        // Get subjects assigned to this faculty
        List<Map<String, Object>> offerings =
                service.getAssignedOfferings(userId);

        /*
         * Add Marks page needs the complete list of students.
         * The Faculty Dashboard uses pagination and filters,
         * but Add Marks does not use those filters.
         *
         * page = 1
         * pageSize = 10000
         * search = ""
         * subjectCode = ""
         * semester = ""
         * academicYear = ""
         */
        List<Map<String, Object>> students =
                service.getAssignedStudents(
                        userId,
                        1,
                        10000,
                        "",
                        "",
                        "",
                        ""
                );

        request.setAttribute("offerings", offerings);
        request.setAttribute("students", students);

        request.getRequestDispatcher("addMarks.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || !"FACULTY".equals(session.getAttribute("role"))) {
            response.sendRedirect("login.jsp");
            return;
        }

        try {
            int studentId =
                    Integer.parseInt(request.getParameter("studentId"));

            int offeringId =
                    Integer.parseInt(request.getParameter("offeringId"));

            int examId =
                    Integer.parseInt(request.getParameter("examId"));

            int userId =
                    (Integer) session.getAttribute("userId");

            int internalMark =
                    Integer.parseInt(request.getParameter("internalMark"));

            int externalMark =
                    Integer.parseInt(request.getParameter("externalMark"));

            Marks marks = new Marks();

            marks.setStudId(studentId);
            marks.setInternalMark(internalMark);
            marks.setExternalMark(externalMark);

            MarksService service = new MarksService();

            boolean success =
                    service.addMarks(
                            marks,
                            studentId,
                            offeringId,
                            examId,
                            userId
                    );

            if (success) {
                response.sendRedirect(
                        "AddMarksServlet?success=true"
                        + "&total=" + marks.getTotalMark()
                        + "&grade=" + marks.getGrade()
                        + "&status=" + marks.getStatus()
                );
            } else {
                response.sendRedirect("AddMarksServlet?error=true");
            }

        } catch (Exception e) {
            System.out.println(e);
            response.sendRedirect("AddMarksServlet?error=true");
        }
    }
}