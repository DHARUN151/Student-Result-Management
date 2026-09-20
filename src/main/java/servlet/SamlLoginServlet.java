package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import saml.SamlAuthnRequestService;
import saml.SamlConfiguration;

import java.io.IOException;

@WebServlet("/saml/login")
public class SamlLoginServlet extends HttpServlet {

    private final SamlAuthnRequestService authnRequestService =
            new SamlAuthnRequestService();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {

            String requestId =
                    authnRequestService.generateRequestId();

            String relayState =
                    authnRequestService.generateRelayState();

            String authnRequestXml =
                    authnRequestService.createAuthnRequest(
                            requestId
                    );

            String encodedRequest =
                    authnRequestService.deflateAndEncode(
                            authnRequestXml
                    );

            HttpSession session =
                    request.getSession(true);

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

            String redirectUrl =
                    SamlConfiguration.IDP_SSO_URL
                    + "?SAMLRequest="
                    + authnRequestService.urlEncode(
                            encodedRequest
                    )
                    + "&RelayState="
                    + authnRequestService.urlEncode(
                            relayState
                    );

            response.sendRedirect(
                    redirectUrl
            );

        } catch (Exception e) {

            throw new ServletException(
                    "Unable to create SAML AuthnRequest",
                    e
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.sendError(
                HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "GET is required for SAML login"
        );
    }
}