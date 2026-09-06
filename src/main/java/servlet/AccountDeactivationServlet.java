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
@WebServlet("/AccountDeactivationServlet")
public class AccountDeactivationServlet extends HttpServlet{
    private static final long serialVersionUID=1L;
    private AdminService service=new AdminService();
    protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        HttpSession session=request.getSession(false);
        if(session==null||!"ADMIN".equals(session.getAttribute("role"))){
            response.sendRedirect("adminLogin.jsp");
            return;
        }
        try{
            int targetUserId=Integer.parseInt(request.getParameter("userId"));
            Map<String,Object> account=service.getManagedAccount(targetUserId);
            if(account==null){
                response.sendRedirect("ManageAccountsServlet?error=invalidAccount");
                return;
            }
            String role=(String)account.get("role");
            String status=(String)account.get("accountStatus");
            Boolean twoFactorEnabled=(Boolean)account.get("twoFactorEnabled");
            if(!"FACULTY".equals(role)&&!"HOD".equals(role)&&!"EXAM_CELL".equals(role)){
                response.sendRedirect("ManageAccountsServlet?error=invalidAccount");
                return;
            }
            if(!"ACTIVE".equals(status)){
                response.sendRedirect("ManageAccountsServlet?error=alreadyInactive");
                return;
            }
            if(twoFactorEnabled==null||!twoFactorEnabled){
                response.sendRedirect("ManageAccountsServlet?error=noTwoFactor");
                return;
            }
            if(account.get("twoFactorSecret")==null||account.get("twoFactorSecret").toString().isEmpty()){
                response.sendRedirect("ManageAccountsServlet?error=noTwoFactor");
                return;
            }
            request.setAttribute("targetAccount",account);
            request.getRequestDispatcher("deactivateAccount.jsp").forward(request,response);
        }catch(NumberFormatException e){
            response.sendRedirect("ManageAccountsServlet?error=invalidAccount");
        }catch(Exception e){
            System.out.println("Error while preparing account deactivation");
            System.out.println(e.getMessage());
            response.sendRedirect("ManageAccountsServlet?error=invalidAccount");
        }
    }
}