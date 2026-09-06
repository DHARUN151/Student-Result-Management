package servlet;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;
import util.DBConnection;
import util.TwoFactorUtil;
@WebServlet("/PasswordResetServlet")
public class PasswordResetServlet extends HttpServlet{
    private static final long serialVersionUID=1L;
    protected void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        HttpSession session=request.getSession(false);
        if(session==null||session.getAttribute("pendingUserId")==null){
            response.sendRedirect("login.jsp");
            return;
        }
        String password=request.getParameter("password");
        String confirmPassword=request.getParameter("confirmPassword");
        if(password==null||confirmPassword==null||password.length()<8||!password.equals(confirmPassword)){
            response.sendRedirect("resetPassword.jsp?error=true");
            return;
        }
        int userId=(Integer)session.getAttribute("pendingUserId");
        try{
            String secret=TwoFactorUtil.generateSecret();
            String encryptedSecret=TwoFactorUtil.encryptSecret(secret);
            String hashedPassword=BCrypt.hashpw(password,BCrypt.gensalt());
            String sql="UPDATE enterprise.users SET password_hash=?,two_factor_secret=?,two_factor_enabled=false,first_login=true,updated_at=CURRENT_TIMESTAMP WHERE user_id=? AND first_login=true";
            try(Connection connection=DBConnection.getConnection();PreparedStatement statement=connection.prepareStatement(sql)){
                statement.setString(1,hashedPassword);
                statement.setString(2,encryptedSecret);
                statement.setInt(3,userId);
                int updated=statement.executeUpdate();
                if(updated==1){
                    session.setAttribute("twoFactorSecret",secret);
                    session.setAttribute("passwordResetCompleted",true);
                    response.sendRedirect("TwoFactorSetupServlet");
                }else{
                    response.sendRedirect("login.jsp?error=true");
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            response.sendRedirect("resetPassword.jsp?error=true");
        }
    }
}