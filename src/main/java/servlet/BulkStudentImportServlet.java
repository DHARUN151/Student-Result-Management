package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import service.AuthorizationService;
import service.BulkStudentImportService;

@WebServlet("/BulkStudentImportServlet")
@MultipartConfig(
        fileSizeThreshold=1024*1024,
        maxFileSize=5*1024*1024,
        maxRequestSize=6*1024*1024
)
public class BulkStudentImportServlet extends HttpServlet {
    private AuthorizationService authorizationService=
            new AuthorizationService();

    private BulkStudentImportService bulkStudentImportService=
            new BulkStudentImportService();

    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,IOException {

        HttpSession session=request.getSession(false);

        if(session==null || session.getAttribute("userId")==null) {
            response.sendRedirect(
                    request.getContextPath()+"/login.jsp");
            return;
        }

        int userId=(Integer)session.getAttribute("userId");

        if(!authorizationService.hasPermission(
                userId,"STUDENT_IMPORT")) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You do not have permission to import students.");
            return;
        }

        request.getRequestDispatcher(
                "/bulkStudentImport.jsp").forward(request,response);
    }

    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,IOException {

        HttpSession session=request.getSession(false);

        if(session==null || session.getAttribute("userId")==null) {
            response.sendRedirect(
                    request.getContextPath()+"/login.jsp");
            return;
        }

        int userId=(Integer)session.getAttribute("userId");

        if(!authorizationService.hasPermission(
                userId,"STUDENT_IMPORT")) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You do not have permission to import students.");
            return;
        }

        Part filePart=request.getPart("csvFile");

        if(filePart==null ||
           filePart.getSize()==0) {

            request.setAttribute(
                    "errorMessage",
                    "Please select a CSV file.");

            request.getRequestDispatcher(
                    "/bulkStudentImport.jsp").forward(request,response);

            return;
        }

        String fileName=filePart.getSubmittedFileName();

        if(fileName==null ||
           !fileName.toLowerCase().endsWith(".csv")) {

            request.setAttribute(
                    "errorMessage",
                    "Only CSV files are allowed.");

            request.getRequestDispatcher(
                    "/bulkStudentImport.jsp").forward(request,response);

            return;
        }

        String userRole=(String)session.getAttribute("role");

        String ipAddress=request.getRemoteAddr();

        String userAgent=request.getHeader("User-Agent");

        BulkStudentImportService.ImportSummary summary=
                bulkStudentImportService.importCsv(
                        filePart.getInputStream(),
                        userId,
                        userRole,
                        ipAddress,
                        userAgent);

        request.setAttribute("importSummary",summary);

        request.getRequestDispatcher(
                "/bulkStudentImport.jsp").forward(request,response);
    }
}