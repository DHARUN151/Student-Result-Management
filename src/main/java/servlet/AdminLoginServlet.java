package servlet;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.AdminService;
@WebServlet("/adminLogin")
public class AdminLoginServlet extends HttpServlet{
    private static final long serialVersionUID=1L;
    protected void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        String username=request.getParameter("username");
        String password=request.getParameter("password");
        AdminService service=new AdminService();
        if(service.authenticateAdmin(username,password)){
            int userId=service.getAdminUserId(username);
            if(userId==0){
                response.sendRedirect("adminLogin.jsp?error=true");
                return;
            }
            HttpSession oldSession=request.getSession(false);
            if(oldSession!=null){
                oldSession.invalidate();
            }
            HttpSession session=request.getSession(true);
            session.setMaxInactiveInterval(30*60);
            session.setAttribute("userId",userId);
            session.setAttribute("username",username.trim());
            session.setAttribute("role","ADMIN");
            session.setAttribute("pendingUserId",userId);
            session.setAttribute("pendingUsername",username.trim());
            response.sendRedirect("TwoFactorLoginServlet");
        }else{
            response.sendRedirect("adminLogin.jsp?error=true");
        }
    }
}