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
import service.FacultySubjectService;

@WebServlet("/FacultyDashboardServlet")
public class FacultyDashboardServlet extends HttpServlet {
    private static final long serialVersionUID=1L;

    private FacultySubjectService service=new FacultySubjectService();

    @Override
    protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException {

        HttpSession session=request.getSession(false);

        if(session==null||session.getAttribute("userId")==null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String role=(String)session.getAttribute("role");

        if(!"FACULTY".equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,"Access denied.");
            return;
        }

        Object userIdObject=session.getAttribute("userId");

        if(!(userIdObject instanceof Integer)) {
            response.sendRedirect("login.jsp");
            return;
        }

        int userId=(Integer)userIdObject;


        /*
         * ========================================================
         * PAGE
         * ========================================================
         */

        int page=1;

        String pageParameter=request.getParameter("page");

        if(pageParameter!=null&&!pageParameter.trim().isEmpty()) {

            try {
                page=Integer.parseInt(pageParameter);
            } catch(NumberFormatException e) {
                page=1;
            }
        }

        if(page<1) {
            page=1;
        }


        /*
         * ========================================================
         * PAGE SIZE
         * ========================================================
         */

        int pageSize=20;

        String pageSizeParameter=request.getParameter("pageSize");

        if(pageSizeParameter!=null&&!pageSizeParameter.trim().isEmpty()) {

            try {
                pageSize=Integer.parseInt(pageSizeParameter);
            } catch(NumberFormatException e) {
                pageSize=20;
            }
        }

        if(pageSize!=10&&pageSize!=20&&pageSize!=50) {
            pageSize=20;
        }


        /*
         * ========================================================
         * FILTERS
         * ========================================================
         */

        String search=request.getParameter("search");
        String subjectCode=request.getParameter("subjectCode");
        String semester=request.getParameter("semester");
        String academicYear=request.getParameter("academicYear");

        if(search==null) {
            search="";
        } else {
            search=search.trim();
        }

        if(subjectCode==null) {
            subjectCode="";
        } else {
            subjectCode=subjectCode.trim();
        }

        if(semester==null) {
            semester="";
        } else {
            semester=semester.trim();
        }

        if(academicYear==null) {
            academicYear="";
        } else {
            academicYear=academicYear.trim();
        }


        /*
         * ========================================================
         * TOTAL FILTERED RECORDS
         * ========================================================
         */

        int totalRecords=service.getAssignedStudentCount(
            userId,
            search,
            subjectCode,
            semester,
            academicYear
        );


        /*
         * ========================================================
         * TOTAL PAGES
         * ========================================================
         *
         * IMPORTANT:
         * Use the SAME pageSize that the user selected.
         */

        int totalPages=0;

        if(totalRecords>0) {
            totalPages=(int)Math.ceil((double)totalRecords/(double)pageSize);
        }


        /*
         * ========================================================
         * NO RECORDS
         * ========================================================
         */

        if(totalPages==0) {
            totalPages=1;
        }


        /*
         * ========================================================
         * KEEP PAGE WITHIN RANGE
         * ========================================================
         */

        if(page>totalPages) {
            page=totalPages;
        }

        if(page<1) {
            page=1;
        }


        /*
         * ========================================================
         * GET PAGINATED + FILTERED STUDENTS
         * ========================================================
         */

        List<Map<String,Object>> students=service.getAssignedStudents(
            userId,
            page,
            pageSize,
            search,
            subjectCode,
            semester,
            academicYear
        );


        /*
         * ========================================================
         * GET ALL FACULTY ASSIGNMENTS
         *
         * Used only for dropdown options.
         * This list is NOT paginated.
         * ========================================================
         */

        List<Map<String,Object>> assignedOfferings=service.getAssignedOfferings(userId);


        /*
         * ========================================================
         * REQUEST ATTRIBUTES
         * ========================================================
         */

        request.setAttribute("students",students);

        request.setAttribute("assignedOfferings",assignedOfferings);

        request.setAttribute("page",page);

        request.setAttribute("pageSize",pageSize);

        request.setAttribute("totalRecords",totalRecords);

        request.setAttribute("totalPages",totalPages);

        request.setAttribute("search",search);

        request.setAttribute("subjectCode",subjectCode);

        request.setAttribute("semester",semester);

        request.setAttribute("academicYear",academicYear);


        /*
         * ========================================================
         * FORWARD TO JSP
         * ========================================================
         */

        request.getRequestDispatcher("teacherDashboard.jsp").forward(request,response);
    }
}