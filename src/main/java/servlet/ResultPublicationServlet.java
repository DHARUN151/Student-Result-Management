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

import service.ExamCellAnalysisService;
import service.NotificationService;

@WebServlet("/ResultPublicationServlet")
public class ResultPublicationServlet extends HttpServlet {

    private static final long serialVersionUID=1L;

    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,IOException {

        HttpSession session=
                request.getSession(false);

        /*
         * Only Exam Cell can access
         * result publication.
         */
        if(session==null ||
           !"EXAM_CELL".equals(session.getAttribute("role"))) {

            response.sendRedirect("login.jsp");

            return;
        }

        ExamCellAnalysisService service=
                new ExamCellAnalysisService();

        List<Map<String,Object>> departments=
                service.getApprovedDepartments();

        /*
         * Calculate publication status for
         * every approved department.
         */
        for(Map<String,Object> department:departments) {

            int departmentId=
                    ((Number)department.get(
                            "departmentId")).intValue();

            Map<String,Integer> completeness=
                    service.getPublicationCompleteness(
                            departmentId);

            int incompleteGroups=
                    completeness.get(
                            "incompleteGroups");

            int missingMarks=
                    completeness.get(
                            "missingMarks");

            department.put(
                    "incompleteGroups",
                    incompleteGroups);

            department.put(
                    "missingMarks",
                    missingMarks);

            boolean blocked=
                    incompleteGroups>0 ||
                    missingMarks>0;

            department.put(
                    "publicationBlocked",
                    blocked);
        }

        request.setAttribute(
                "departments",
                departments);

        request.setAttribute(
                "published",
                request.getParameter("published"));

        request.setAttribute(
                "error",
                request.getParameter("error"));

        request.setAttribute(
                "blocked",
                request.getParameter("blocked"));

        request.getRequestDispatcher(
                "resultPublication.jsp")
                .forward(request,response);
    }

    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,IOException {

        HttpSession session=
                request.getSession(false);

        /*
         * Only Exam Cell can publish.
         */
        if(session==null ||
           !"EXAM_CELL".equals(session.getAttribute("role"))) {

            response.sendRedirect("login.jsp");

            return;
        }

        try {

            int departmentId=
                    Integer.parseInt(
                            request.getParameter(
                                    "departmentId"));

            int userId=
                    (Integer)session.getAttribute(
                            "userId");

            String remarks=
                    request.getParameter("remarks");

            ExamCellAnalysisService service=
                    new ExamCellAnalysisService();

            /*
             * Phase 8:
             * Perform completeness check before
             * attempting publication.
             */
            Map<String,Integer> completeness=
                    service.getPublicationCompleteness(
                            departmentId);

            int incompleteGroups=
                    completeness.get(
                            "incompleteGroups");

            int missingMarks=
                    completeness.get(
                            "missingMarks");

            /*
             * Stop publication when marks are missing.
             */
            if(incompleteGroups>0 ||
               missingMarks>0) {

                response.sendRedirect(
                        "ResultPublicationServlet"
                        +"?blocked=true"
                        +"&departmentId="
                        +departmentId);

                return;
            }

            /*
             * The DAO performs the same check again
             * inside the database transaction.
             *
             * This protects against bypassing the JSP.
             */
            boolean published=
                    service.publishDepartmentResults(
                            departmentId,
                            userId,
                            remarks);

            if(published) {

                /*
                 * Phase 11:
                 * Notify students after successful publication.
                 */
                List<Map<String,Object>> publishedResults =
                        service.getPublishedResultsForDepartment(
                                departmentId);

                NotificationService notificationService =
                        new NotificationService();

                for(Map<String,Object> result:
                        publishedResults) {

                    int studentId =
                            ((Number)result.get(
                                    "studentId")).intValue();

                    int resultId =
                            ((Number)result.get(
                                    "resultId")).intValue();

                    int semester =
                            ((Number)result.get(
                                    "semester")).intValue();

                    String studentName =
                            (String)result.get(
                                    "studentName");

                    String studentEmail =
                            (String)result.get(
                                    "studentEmail");

                    String resultLink =
                            request.getScheme()
                            + "://"
                            + request.getServerName()
                            + ":"
                            + request.getServerPort()
                            + request.getContextPath()
                            + "/viewResult";

                    notificationService.notifyResultPublished(
                            studentId,
                            resultId,
                            studentEmail,
                            studentName,
                            semester,
                            resultLink);
                }

                response.sendRedirect(
                        "ResultPublicationServlet"
                        +"?published=true");

            } else {

                response.sendRedirect(
                        "ResultPublicationServlet"
                        +"?error=true");
            }

        } catch(Exception e) {

            e.printStackTrace();

            response.sendRedirect(
                    "ResultPublicationServlet"
                    +"?error=true");
        }
    }
    NotificationService notificationService =
            new NotificationService();
}