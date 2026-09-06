package servlet;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.AdminService;
@WebServlet("/ManageAccountsServlet")
public class ManageAccountsServlet extends HttpServlet{
    private static final long serialVersionUID=1L;
    private AdminService service=new AdminService();
    protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        HttpSession session=request.getSession(false);
        if(session==null||!"ADMIN".equals(session.getAttribute("role"))){
            response.sendRedirect("adminLogin.jsp");
            return;
        }
        List<Map<String,Object>> accounts=service.getManagedAccounts();
        request.setAttribute("accounts",accounts);
        String success=request.getParameter("success");
        String error=request.getParameter("error");
        if("deactivated".equals(success)){
            request.setAttribute("message","Account deactivated successfully.");
            request.setAttribute("messageType","success");
        }else if("invalidAccount".equals(error)){
            request.setAttribute("message","Invalid account selected.");
            request.setAttribute("messageType","error");
        }else if("alreadyInactive".equals(error)){
            request.setAttribute("message","This account is already inactive.");
            request.setAttribute("messageType","error");
        }else if("noTwoFactor".equals(error)){
            request.setAttribute("message","This account does not have valid two-factor authentication configured.");
            request.setAttribute("messageType","error");
        }else if("deactivationFailed".equals(error)){
            request.setAttribute("message","Account deactivation failed.");
            request.setAttribute("messageType","error");
        }
        request.getRequestDispatcher("manageAccounts.jsp").forward(request,response);
    }
}