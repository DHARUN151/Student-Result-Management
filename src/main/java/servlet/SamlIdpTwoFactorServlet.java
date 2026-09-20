package servlet;

import dao.UserDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import model.User;

import service.SamlResponseService;
import util.TwoFactorUtil;

import java.io.IOException;

@WebServlet("/saml/idp/2fa")
public class SamlIdpTwoFactorServlet
        extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final UserDAO userDAO =
            new UserDAO();

    private SamlResponseService
            samlResponseService;
    
    @Override
    public void init()
            throws ServletException {

        samlResponseService =
                new SamlResponseService(
                        getServletContext()
                );
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);

        if (!isValidSamlSession(session)) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/login.jsp?error=session"
            );

            return;
        }

        request.getRequestDispatcher(
                "/samlIdpTwoFactor.jsp"
        ).forward(
                request,
                response
        );
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);

        if (!isValidSamlSession(session)) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/login.jsp?error=session"
            );

            return;
        }

        Integer userId =
                (Integer) session.getAttribute(
                        "SAML_PENDING_USER_ID"
                );

        String username =
                (String) session.getAttribute(
                        "SAML_PENDING_USERNAME"
                );

        String code =
                request.getParameter("code");

        if (userId == null
                || username == null
                || code == null
                || code.trim().isEmpty()) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/saml/idp/2fa?error=true"
            );

            return;
        }

        User user =
                userDAO.findUserByUsername(
                        username
                );

        if (user == null
                || user.getUserId() != userId
                || !"ACTIVE".equals(
                        user.getAccountStatus())
                || !isPrivilegedRole(
                        user.getRole())
                || !user.isTwoFactorEnabled()
                || user.getTwoFactorSecret() == null
                || user.getTwoFactorSecret()
                        .trim()
                        .isEmpty()) {

            session.invalidate();

            response.sendRedirect(
                    request.getContextPath()
                    + "/login.jsp?error=authentication"
            );

            return;
        }

        try {

            String secret =
                    TwoFactorUtil.decryptSecret(
                            user.getTwoFactorSecret()
                    );

            boolean validCode =
                    TwoFactorUtil.verifyCode(
                            secret,
                            code.trim()
                    );

            if (!validCode) {

                response.sendRedirect(
                        request.getContextPath()
                        + "/saml/idp/2fa?error=true"
                );

                return;
            }

            /*
             * 2FA is successful.
             *
             * Do NOT create the final application
             * session here.
             *
             * Generate the SAML Response and send it
             * to the SP's ACS endpoint.
             */
            String samlResponse =
                    samlResponseService
                            .createSignedResponse(
                                    user,
                                    session
                            );

            String relayState =
                    (String) session.getAttribute(
                            "SAML_RELAY_STATE"
                    );

            samlResponseService
                    .sendResponseToAcs(
                            response,
                            samlResponse,
                            relayState
                    );

        } catch (Exception e) {

            e.printStackTrace();

            session.invalidate();

            response.sendRedirect(
                    request.getContextPath()
                    + "/login.jsp?error=saml"
            );
        }
    }

    private boolean isValidSamlSession(
            HttpSession session) {

        return session != null
                && Boolean.TRUE.equals(
                        session.getAttribute(
                                "SAML_FLOW_ACTIVE"
                        )
                )
                && session.getAttribute(
                        "SAML_REQUEST_ID"
                ) != null
                && session.getAttribute(
                        "SAML_PENDING_USER_ID"
                ) != null
                && session.getAttribute(
                        "SAML_PENDING_USERNAME"
                ) != null;
    }

    private boolean isPrivilegedRole(
            String role) {

        return "ADMIN".equals(role)
                || "SUPER_ADMIN".equals(role)
                || "FACULTY".equals(role)
                || "HOD".equals(role)
                || "EXAM_CELL".equals(role);
    }
}