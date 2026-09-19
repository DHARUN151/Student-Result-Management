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

import service.DigiLockerService;
import service.MarksheetVerificationService;

@WebServlet("/DigiLockerManagementServlet")
public class DigiLockerManagementServlet extends HttpServlet {

    private static final long serialVersionUID=1L;

    private final DigiLockerService digiLockerService=
            new DigiLockerService();

    private final MarksheetVerificationService
            marksheetVerificationService=
            new MarksheetVerificationService();

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
                    "You do not have permission to access DigiLocker management.");

            return;
        }

        loadPage(
                request,
                response,
                null,
                null);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,IOException {

        HttpSession session=
                request.getSession(false);

        if(!isAuthorized(session)) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You do not have permission to manage DigiLocker documents.");

            return;
        }

        String action=
                request.getParameter("action");

        if(action==null ||
           action.trim().isEmpty()) {

            loadPage(
                    request,
                    response,
                    "Invalid DigiLocker action.",
                    null);

            return;
        }

        if("prepare".equalsIgnoreCase(action)) {

            prepareDocument(
                    request,
                    response);

            return;
        }

        if("markAvailable".equalsIgnoreCase(action)) {

            markAvailable(
                    request,
                    response);

            return;
        }

        loadPage(
                request,
                response,
                "Unknown DigiLocker action.",
                null);
    }

    private void prepareDocument(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,IOException {

        int resultId;

        try {

            resultId=
                    Integer.parseInt(
                            request.getParameter(
                                    "resultId"));

        } catch(Exception e) {

            loadPage(
                    request,
                    response,
                    "Invalid result ID.",
                    null);

            return;
        }

        List<Map<String,Object>> results=
                digiLockerService
                        .getPublishedResultsForPreparation();

        Map<String,Object> selectedResult=null;

        for(Map<String,Object> result :
                results) {

            Object idObject=
                    result.get("resultId");

            if(idObject==null) {
                continue;
            }

            try {

                int currentId=
                        Integer.parseInt(
                                idObject.toString());

                if(currentId==resultId) {

                    selectedResult=result;

                    break;
                }

            } catch(Exception e) {

                // Ignore invalid result ID.
            }
        }

        /*
         * Never prepare a result which is not
         * returned by the PUBLISHED-result query.
         */
        if(selectedResult==null) {

            loadPage(
                    request,
                    response,
                    "Only published results can be prepared for DigiLocker.",
                    null);

            return;
        }

        int studentId=
                Integer.parseInt(
                        selectedResult
                                .get("studentId")
                                .toString());

        int semester=
                Integer.parseInt(
                        selectedResult
                                .get("semester")
                                .toString());

        /*
         * Obtain an existing verification code.
         *
         * If one does not exist, create it first.
         */
        String verificationCode=
                marksheetVerificationService
                        .createOrGetVerificationCode(
                                resultId,
                                semester);

        if(verificationCode==null ||
           verificationCode.trim().isEmpty()) {

            loadPage(
                    request,
                    response,
                    "Unable to create the marksheet verification code.",
                    null);

            return;
        }

        boolean success=
                digiLockerService.prepareDocument(
                        resultId,
                        studentId,
                        semester,
                        verificationCode);

        if(success) {

            loadPage(
                    request,
                    response,
                    null,
                    "DigiLocker document prepared successfully for result "
                    +resultId+".");

        } else {

            loadPage(
                    request,
                    response,
                    "Unable to prepare DigiLocker document for result "
                    +resultId+".",
                    null);
        }
    }

    private void markAvailable(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,IOException {

        int resultId;

        try {

            resultId=
                    Integer.parseInt(
                            request.getParameter(
                                    "resultId"));

        } catch(Exception e) {

            loadPage(
                    request,
                    response,
                    "Invalid result ID.",
                    null);

            return;
        }

        /*
         * IMPORTANT:
         *
         * This action represents the institution
         * confirming that the document has already
         * been successfully published through the
         * official DigiLocker/NAD process.
         *
         * It does NOT call DigiLocker itself.
         */
        boolean success=
                digiLockerService.markAvailable(
                        resultId);

        if(success) {

            loadPage(
                    request,
                    response,
                    null,
                    "Document marked as AVAILABLE for result "
                    +resultId+".");

        } else {

            loadPage(
                    request,
                    response,
                    "Document could not be marked as AVAILABLE. "
                    +"Make sure it has already been submitted.",
                    null);
        }
    }

    private void loadPage(
            HttpServletRequest request,
            HttpServletResponse response,
            String errorMessage,
            String successMessage)
            throws ServletException,IOException {

        List<Map<String,Object>> results=
                digiLockerService
                        .getPublishedResultsForPreparation();

        request.setAttribute(
                "publishedResults",
                results);

        request.setAttribute(
                "errorMessage",
                errorMessage);

        request.setAttribute(
                "successMessage",
                successMessage);

        request.getRequestDispatcher(
                "/digilockerManagement.jsp")
                .forward(
                        request,
                        response);
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
                || "SUPER_ADMIN".equals(role)
                || "EXAM_CELL".equals(role);
    }
}