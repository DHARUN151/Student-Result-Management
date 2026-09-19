package servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import util.DBConnection;
import util.TwoFactorUtil;

@WebServlet("/TwoFactorLoginServlet")
public class TwoFactorLoginServlet extends HttpServlet {
    private static final long serialVersionUID=1L;

    protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException {
        HttpSession session=request.getSession(false);

        if(session==null||session.getAttribute("pendingUserId")==null) {
            response.sendRedirect("login.jsp");
            return;
        }

        request.getRequestDispatcher("twoFactorLogin.jsp").forward(request,response);
    }

    protected void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException {
        HttpSession session=request.getSession(false);

        if(session==null||session.getAttribute("pendingUserId")==null) {
            response.sendRedirect("login.jsp");
            return;
        }

        int userId=(Integer)session.getAttribute("pendingUserId");
        String code=request.getParameter("code");
        String encryptedSecret=null;
        String role=null;

        String sql="SELECT u.two_factor_secret,u.two_factor_enabled,r.role_name FROM enterprise.users u JOIN enterprise.user_roles ur ON u.user_id=ur.user_id JOIN enterprise.roles r ON ur.role_id=r.role_id WHERE u.user_id=? AND u.account_status='ACTIVE'";

        try(Connection connection=DBConnection.getConnection();PreparedStatement statement=connection.prepareStatement(sql)) {
            statement.setInt(1,userId);

            try(ResultSet result=statement.executeQuery()) {
                if(result.next()) {
                    encryptedSecret=result.getString("two_factor_secret");
                    boolean enabled=result.getBoolean("two_factor_enabled");
                    role=result.getString("role_name");

                    if(!enabled||encryptedSecret==null||encryptedSecret.isEmpty()) {
                        session.invalidate();
                        response.sendRedirect("login.jsp?error=true");
                        return;
                    }
                } else {
                    session.invalidate();
                    response.sendRedirect("login.jsp?error=true");
                    return;
                }
            }
        } catch(Exception e) {
            System.out.println("Error while loading two factor information");
            System.out.println(e.getMessage());
            session.invalidate();
            response.sendRedirect("login.jsp?error=true");
            return;
        }

        try {
            String secret=TwoFactorUtil.decryptSecret(encryptedSecret);

            if(!TwoFactorUtil.verifyCode(secret,code)) {
                response.sendRedirect("TwoFactorLoginServlet?error=true");
                return;
            }
        } catch(Exception e) {
            System.out.println("Error while verifying two factor code");
            System.out.println(e.getMessage());
            session.invalidate();
            response.sendRedirect("login.jsp?error=true");
            return;
        }

        session.removeAttribute("pendingUserId");
        session.removeAttribute("pendingUsername");
        session.removeAttribute("twoFactorSecret");
        session.removeAttribute("passwordResetCompleted");

        if("FACULTY".equals(role)) {
            response.sendRedirect("FacultyDashboardServlet");
        } else if("HOD".equals(role)) {
            response.sendRedirect("HODResultServlet");
        } else if("EXAM_CELL".equals(role)) {
            response.sendRedirect("ExamCellDashboardServlet");
        } else if("ADMIN".equals(role)||"SUPER_ADMIN".equals(role)) {
            response.sendRedirect("AdminServlet");
        } else {
            session.invalidate();
            response.sendRedirect("login.jsp");
        }
    }
}