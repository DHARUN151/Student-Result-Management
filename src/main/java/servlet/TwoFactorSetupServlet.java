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
import util.DBConnection;
import util.TwoFactorUtil;
@WebServlet("/TwoFactorSetupServlet")
public class TwoFactorSetupServlet extends HttpServlet{
    private static final long serialVersionUID=1L;
    protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        HttpSession session=request.getSession(false);
        if(session==null||session.getAttribute("pendingUserId")==null||session.getAttribute("twoFactorSecret")==null){
            response.sendRedirect("login.jsp");
            return;
        }
        request.getRequestDispatcher("twoFactorSetup.jsp").forward(request,response);
    }
    protected void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        HttpSession session=request.getSession(false);
        if(session==null||session.getAttribute("pendingUserId")==null||session.getAttribute("twoFactorSecret")==null){
            response.sendRedirect("login.jsp");
            return;
        }
        String code=request.getParameter("code");
        String secret=(String)session.getAttribute("twoFactorSecret");
        int userId=(Integer)session.getAttribute("pendingUserId");
        if(!TwoFactorUtil.verifyCode(secret,code)){
            response.sendRedirect("TwoFactorSetupServlet?error=true");
            return;
        }
        String sql="UPDATE enterprise.users SET two_factor_enabled=true,first_login=false,updated_at=CURRENT_TIMESTAMP WHERE user_id=? AND first_login=true AND two_factor_enabled=false";
        try(Connection connection=DBConnection.getConnection();PreparedStatement statement=connection.prepareStatement(sql)){
            statement.setInt(1,userId);
            int updated=statement.executeUpdate();
            if(updated==1){
                session.invalidate();
                response.sendRedirect("login.jsp?setup=success");
            }else{
                response.sendRedirect("login.jsp?error=true");
            }
        }catch(Exception e){
            e.printStackTrace();
            response.sendRedirect("TwoFactorSetupServlet?error=true");
        }
    }
}