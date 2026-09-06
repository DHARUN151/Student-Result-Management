package servlet;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet{
    private static final long serialVersionUID=1L;
    protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        HttpSession session=request.getSession(false);
        String role=null;
        if(session!=null){
            role=(String)session.getAttribute("role");
            session.invalidate();
        }
        if("ADMIN".equals(role)){
            response.sendRedirect("adminLogin.jsp");
        }else{
            response.sendRedirect("login.jsp");
        }
    }
}