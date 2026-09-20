package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import service.SamlRequestValidationService;
import service.SamlRequestValidationService.SamlRequestData;

import java.io.IOException;

@WebServlet("/saml/idp/sso")
public class SamlIdpSsoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private SamlRequestValidationService
            requestValidationService;

    @Override
    public void init() throws ServletException {

        requestValidationService =
                new SamlRequestValidationService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String samlRequest =
                request.getParameter("SAMLRequest");

        String relayState =
                request.getParameter("RelayState");

        HttpSession session =
                request.getSession(false);

        /*
         * The request must originate from
         * our existing login flow.
         */
        if (session == null
                || !Boolean.TRUE.equals(
                        session.getAttribute(
                                "SAML_FLOW_ACTIVE"
                        )
                )
                || session.getAttribute(
                        "SAML_PENDING_USER_ID"
                ) == null) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/login.jsp?error=saml"
            );

            return;
        }

        try {

            SamlRequestData requestData =
                    requestValidationService
                            .validateRedirectRequest(
                                    samlRequest
                            );

            /*
             * Verify that the request belongs to
             * the same SAML transaction stored
             * in the login session.
             */
            String sessionRequestId =
                    (String) session.getAttribute(
                            "SAML_REQUEST_ID"
                    );

            if (sessionRequestId == null
                    || !sessionRequestId.equals(
                            requestData.getRequestId()
                    )) {

                session.invalidate();

                response.sendRedirect(
                        request.getContextPath()
                        + "/login.jsp?error=saml"
                );

                return;
            }

            session.setAttribute(
                    "SAML_SP_ENTITY_ID",
                    requestData.getIssuer()
            );

            session.setAttribute(
                    "SAML_ACS_URL",
                    requestData.getAcsUrl()
            );

            session.setAttribute(
                    "SAML_RELAY_STATE",
                    relayState
            );

            session.setAttribute(
                    "SAML_REQUEST_ISSUE_INSTANT",
                    requestData.getIssueInstant()
            );

            /*
             * IMPORTANT:
             *
             * Do NOT display username/password.
             *
             * Go directly to the second-factor
             * verification page.
             */
            response.sendRedirect(
                    request.getContextPath()
                    + "/saml/idp/2fa"
            );

        } catch (Exception e) {

            e.printStackTrace();

            session.invalidate();

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid SAML authentication request"
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        doGet(
                request,
                response
        );
    }
}