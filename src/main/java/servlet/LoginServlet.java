package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import model.User;
import service.LoginService;
import service.SamlIdpAuthenticationService;
import saml.SamlAuthnRequestService;
import saml.SamlConfiguration;
import dao.StudentDAO;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private LoginService loginService;

    private SamlAuthnRequestService samlAuthnRequestService;

    private SamlIdpAuthenticationService samlIdpAuthenticationService;

    @Override
    public void init() throws ServletException {

        loginService = new LoginService();

        samlAuthnRequestService =
                new SamlAuthnRequestService();

        samlIdpAuthenticationService =
                new SamlIdpAuthenticationService();
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String username =
                request.getParameter("username");

        String password =
                request.getParameter("password");

        User user =
                loginService.login(
                        username,
                        password
                );

        if (user == null) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/login.jsp?error=invalid"
            );

            return;
        }

        String role =
                user.getRole();

        /*
         * STUDENT LOGIN
         *
         * Students do not use SAML.
         */
        if ("STUDENT".equals(role)) {

            HttpSession oldSession =
                    request.getSession(false);

            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession session =
                    request.getSession(true);

            session.setMaxInactiveInterval(30 * 60);

            session.setAttribute(
                    "userId",
                    user.getUserId()
            );

            session.setAttribute(
                    "username",
                    user.getUsername()
            );

            session.setAttribute(
                    "role",
                    user.getRole()
            );

            StudentDAO studentDAO =
                    new StudentDAO();

            Integer studentId =
                    studentDAO.getStudentIdByUserId(
                            user.getUserId()
                    );

            if (studentId == null) {

                response.sendRedirect(
                        request.getContextPath()
                        + "/login.jsp?error=student"
                );

                return;
            }

            session.setAttribute(
                    "studentId",
                    studentId
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/StudentDashboardServlet"
            );

            return;
        }

        /*
         * PRIVILEGED ROLES
         *
         * SAML is required for:
         *
         * ADMIN
         * SUPER_ADMIN
         * FACULTY
         * HOD
         * EXAM_CELL
         */
        if (samlIdpAuthenticationService
                .isPrivilegedRole(role)) {

            startPrivilegedSamlFlow(
                    request,
                    response,
                    user
            );

            return;
        }

        response.sendRedirect(
                request.getContextPath()
                + "/login.jsp?error=role"
        );
    }

    private void startPrivilegedSamlFlow(
            HttpServletRequest request,
            HttpServletResponse response,
            User user)
            throws IOException {

        /*
         * Create a fresh session.
         */
        HttpSession oldSession =
                request.getSession(false);

        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession session =
                request.getSession(true);

        session.setMaxInactiveInterval(
                30 * 60
        );

        /*
         * Do NOT create the final application
         * authentication session yet.
         *
         * Store only pending authentication data.
         */
        session.setAttribute(
                "SAML_FLOW_ACTIVE",
                true
        );

        session.setAttribute(
                "SAML_PENDING_USER_ID",
                user.getUserId()
        );

        session.setAttribute(
                "SAML_PENDING_USERNAME",
                user.getUsername()
        );

        session.setAttribute(
                "SAML_PENDING_ROLE",
                user.getRole()
        );

        /*
         * Generate SAML AuthnRequest.
         */
        try {

            String requestId =
                    samlAuthnRequestService
                            .generateRequestId();

            String relayState =
                    samlAuthnRequestService
                            .generateRelayState();

            String authnRequestXml =
                    samlAuthnRequestService
                            .createAuthnRequest(
                                    requestId
                            );

            String encodedRequest =
                    samlAuthnRequestService
                            .deflateAndEncode(
                                    authnRequestXml
                            );

            session.setAttribute(
                    "SAML_REQUEST_ID",
                    requestId
            );

            session.setAttribute(
                    "SAML_RELAY_STATE",
                    relayState
            );

            session.setAttribute(
                    "SAML_AUTHN_REQUEST_TIME",
                    System.currentTimeMillis()
            );

            /*
             * Redirect to the IdP SSO endpoint.
             *
             * No SAML username/password page is shown.
             */
            String redirectUrl =
                    SamlConfiguration.IDP_SSO_URL
                    + "?SAMLRequest="
                    + samlAuthnRequestService
                            .urlEncode(encodedRequest)
                    + "&RelayState="
                    + samlAuthnRequestService
                            .urlEncode(relayState);

            response.sendRedirect(
                    redirectUrl
            );

        } catch (Exception e) {

            throw new IOException(
                    "Unable to start SAML authentication",
                    e
            );
        }
    }
}