package servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import service.DigiLockerService;
import service.StudentService;

@WebServlet("/DigiLockerServlet")
public class DigiLockerServlet extends HttpServlet {

    private static final long serialVersionUID=1L;

    private DigiLockerService digiLockerService;

    private StudentService studentService;

    @Override
    public void init()
            throws ServletException {

        digiLockerService=
                new DigiLockerService();

        studentService=
                new StudentService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,IOException {

        HttpSession session=
                request.getSession(false);

        /*
         * Student-only access.
         */
        if(session==null ||
           session.getAttribute("userId")==null ||
           !"STUDENT".equals(
                   session.getAttribute("role"))) {

            response.sendRedirect(
                    request.getContextPath()
                    +"/login.jsp");

            return;
        }

        Object studentIdObject=
                session.getAttribute(
                        "studentId");

        if(studentIdObject==null) {

            response.sendRedirect(
                    request.getContextPath()
                    +"/login.jsp");

            return;
        }

        int studentId;

        try {

            studentId=
                    Integer.parseInt(
                            studentIdObject.toString());

        } catch(Exception e) {

            response.sendRedirect(
                    request.getContextPath()
                    +"/login.jsp");

            return;
        }

        /*
         * Get semesters for which the student
         * has published results.
         */
        List<Integer> publishedSemesters=
                studentService.getPublishedSemesters(
                        studentId);

        /*
         * Get DigiLocker records already
         * prepared by the institution.
         */
        List<Map<String,Object>> documents=
                digiLockerService.getStudentDocuments(
                        studentId);

        /*
         * Convert the document list into a map
         * using semester as the key.
         *
         * This makes JSP lookup simple.
         */
        Map<Integer,Map<String,Object>>
                documentBySemester=
                new HashMap<>();

        if(documents!=null) {

            for(Map<String,Object> document :
                    documents) {

                Object semesterObject=
                        document.get("semester");

                if(semesterObject==null) {
                    continue;
                }

                int semester;

                try {

                    semester=
                            Integer.parseInt(
                                    semesterObject.toString());

                } catch(Exception e) {

                    continue;
                }

                documentBySemester.put(
                        semester,
                        document);
            }
        }

        /*
         * Send data to JSP.
         */
        request.setAttribute(
                "publishedSemesters",
                publishedSemesters);

        request.setAttribute(
                "documents",
                documents);

        request.setAttribute(
                "documentBySemester",
                documentBySemester);

        /*
         * DigiLocker is an institution-side
         * publication process.
         *
         * Until the official NAD publication
         * is completed, the student sees the
         * correct status instead of a fake link.
         */
        request.setAttribute(
                "integrationStatus",
                "INSTITUTION_PUBLICATION_REQUIRED");

        request.getRequestDispatcher(
                "/digilocker.jsp")
                .forward(
                        request,
                        response);
    }
}