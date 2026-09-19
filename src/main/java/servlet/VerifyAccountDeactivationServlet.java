package servlet;

import java.io.IOException;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.AdminService;
import service.AuthorizationService;
import util.TwoFactorUtil;

@WebServlet("/VerifyAccountDeactivationServlet")
public class VerifyAccountDeactivationServlet extends HttpServlet {
    private static final long serialVersionUID=1L;
    private AdminService service=new AdminService();
    private AuthorizationService authorizationService=new AuthorizationService();

    protected void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException {
        HttpSession session=request.getSession(false);

        if(session==null||session.getAttribute("userId")==null) {
            response.sendRedirect("adminLogin.jsp");
            return;
        }

        int adminUserId=(Integer)session.getAttribute("userId");

        if(!authorizationService.hasPermission(adminUserId,"USER_DEACTIVATE")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,"You do not have permission to deactivate accounts.");
            return;
        }

        try {
            int targetUserId=Integer.parseInt(request.getParameter("userId"));
            String otp=request.getParameter("otp");

            if(otp==null||!otp.matches("\\d{6}")) {
                request.setAttribute("error","Please enter a valid 6-digit authentication code.");
                Map<String,Object> account=service.getManagedAccount(targetUserId);
                request.setAttribute("targetAccount",account);
                request.getRequestDispatcher("deactivateAccount.jsp").forward(request,response);
                return;
            }

            Map<String,Object> account=service.getManagedAccount(targetUserId);

            if(account==null) {
                response.sendRedirect("ManageAccountsServlet?error=invalidAccount");
                return;
            }

            String role=(String)account.get("role");
            String status=(String)account.get("accountStatus");
            Boolean twoFactorEnabled=(Boolean)account.get("twoFactorEnabled");
            Object secretObject=account.get("twoFactorSecret");

            if(!"FACULTY".equals(role)&&!"HOD".equals(role)&&!"EXAM_CELL".equals(role)) {
                response.sendRedirect("ManageAccountsServlet?error=invalidAccount");
                return;
            }

            if(!"ACTIVE".equals(status)) {
                response.sendRedirect("ManageAccountsServlet?error=alreadyInactive");
                return;
            }

            if(twoFactorEnabled==null||!twoFactorEnabled||secretObject==null||secretObject.toString().isEmpty()) {
                response.sendRedirect("ManageAccountsServlet?error=noTwoFactor");
                return;
            }

            String encryptedSecret=secretObject.toString();
            String secret=TwoFactorUtil.decryptSecret(encryptedSecret);

            if(secret==null||secret.isEmpty()) {
                request.setAttribute("error","Unable to verify the account owner's authenticator.");
                request.setAttribute("targetAccount",account);
                request.getRequestDispatcher("deactivateAccount.jsp").forward(request,response);
                return;
            }

            boolean valid=TwoFactorUtil.verifyCode(secret,otp);

            if(!valid) {
                request.setAttribute("error","Invalid authentication code. Account was not deactivated.");
                request.setAttribute("targetAccount",account);
                request.getRequestDispatcher("deactivateAccount.jsp").forward(request,response);
                return;
            }

            boolean success=service.deactivateAccount(targetUserId,adminUserId);

            if(success) {
                response.sendRedirect("ManageAccountsServlet?success=deactivated");
            } else {
                response.sendRedirect("ManageAccountsServlet?error=deactivationFailed");
            }

        } catch(NumberFormatException e) {
            response.sendRedirect("ManageAccountsServlet?error=invalidAccount");
        } catch(Exception e) {
            System.out.println("Error while verifying account deactivation");
            System.out.println(e.getMessage());
            response.sendRedirect("ManageAccountsServlet?error=deactivationFailed");
        }
    }
}