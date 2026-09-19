package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import service.DigiLockerService;

@WebServlet("/DigiLockerExportServlet")
public class DigiLockerExportServlet extends HttpServlet {

    private static final long serialVersionUID=1L;

    private final DigiLockerService service=
            new DigiLockerService();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,IOException {

        HttpSession session=
                request.getSession(false);

        if(!isAuthorized(session)) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You do not have permission to export DigiLocker data.");

            return;
        }

        List<Map<String,Object>> documents=
                service.getDocumentsForExport();

        response.setContentType(
                "text/csv");

        response.setCharacterEncoding(
                "UTF-8");

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"DigiLocker_NAD_Export.csv\"");

        response.setHeader(
                "Cache-Control",
                "no-store,no-cache,must-revalidate");

        try(PrintWriter writer=
                    response.getWriter()) {

            /*
             * UTF-8 BOM helps Excel correctly
             * recognize the CSV encoding.
             */
            writer.write('\uFEFF');

            /*
             * Internal institutional export.
             *
             * These column names must be mapped to
             * the official NAD template before upload.
             */
            writer.println(
                    "Result ID,"
                    +"Student ID,"
                    +"Register Number,"
                    +"Student Name,"
                    +"Date of Birth,"
                    +"Email,"
                    +"Program,"
                    +"Department,"
                    +"Academic Year,"
                    +"Semester,"
                    +"Examination,"
                    +"Total Marks,"
                    +"Maximum Marks,"
                    +"Percentage,"
                    +"Semester GPA,"
                    +"Overall CGPA,"
                    +"Result Class,"
                    +"Verification Code,"
                    +"Document Status");

            for(Map<String,Object> document :
                    documents) {

                writer.println(
                        csv(document.get("resultId"))
                        +","
                        +csv(document.get("studentId"))
                        +","
                        +csv(document.get("regNum"))
                        +","
                        +csv(document.get("studentName"))
                        +","
                        +csv(document.get("dob"))
                        +","
                        +csv(document.get("email"))
                        +","
                        +csv(document.get("program"))
                        +","
                        +csv(document.get("department"))
                        +","
                        +csv(document.get("academicYear"))
                        +","
                        +csv(document.get("semester"))
                        +","
                        +csv(document.get("examination"))
                        +","
                        +csv(document.get("totalMarks"))
                        +","
                        +csv(document.get("maximumMarks"))
                        +","
                        +csv(document.get("percentage"))
                        +","
                        +csv(document.get("semesterGpa"))
                        +","
                        +csv(document.get("overallCgpa"))
                        +","
                        +csv(document.get("resultClass"))
                        +","
                        +csv(document.get("verificationCode"))
                        +","
                        +csv(document.get("documentStatus")));
            }
        }
    }

    private boolean isAuthorized(
            HttpSession session) {

        if(session==null) {
            return false;
        }

        Object roleObject=
                session.getAttribute("role");

        if(roleObject==null) {
            return false;
        }

        String role=
                roleObject.toString();

        return "ADMIN".equals(role)
                ||"SUPER_ADMIN".equals(role)
                ||"EXAM_CELL".equals(role);
    }

    private String csv(
            Object value) {

        if(value==null) {
            return "";
        }

        String text=
                value.toString();

        /*
         * Escape CSV special characters.
         */
        text=
                text.replace(
                        "\"",
                        "\"\"");

        if(text.contains(",")
                ||text.contains("\"")
                ||text.contains("\n")
                ||text.contains("\r")) {

            return "\""+text+"\"";
        }

        return text;
    }
}