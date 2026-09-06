package servlet;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.AdminService;
@WebServlet("/AdminServlet")
public class AdminServlet extends HttpServlet{
    private static final long serialVersionUID=1L;
    private AdminService service=new AdminService();
    protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        HttpSession session=request.getSession(false);
        if(session==null||!"ADMIN".equals(session.getAttribute("role"))){
            response.sendRedirect("adminLogin.jsp");
            return;
        }
        String action=request.getParameter("action");
        String role=request.getParameter("role");
        if("create".equals(action)){
            if(!service.isAllowedRole(role)){
                response.sendError(HttpServletResponse.SC_FORBIDDEN,"This account type cannot be created from Admin Portal.");
                return;
            }
            request.setAttribute("createRole",role);
            request.getRequestDispatcher("createAccount.jsp").forward(request,response);
            return;
        }
        request.getRequestDispatcher("adminDashboard.jsp").forward(request,response);
    }
    protected void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        HttpSession session=request.getSession(false);
        if(session==null||!"ADMIN".equals(session.getAttribute("role"))){
            response.sendRedirect("adminLogin.jsp");
            return;
        }
        String action=request.getParameter("action");
        if(!"createAccount".equals(action)){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Invalid request.");
            return;
        }
        String role=request.getParameter("role");
        if(!service.isAllowedRole(role)){
            response.sendError(HttpServletResponse.SC_FORBIDDEN,"This account type cannot be created from Admin Portal.");
            return;
        }
        String employeeCode=request.getParameter("employeeCode");
        String name=request.getParameter("name");
        String email=request.getParameter("email");
        String phone=request.getParameter("phone");
        String departmentId=request.getParameter("departmentId");
        String designation=request.getParameter("designation");
        String password=request.getParameter("password");
        boolean created=service.createAccount(employeeCode,name,email,phone,departmentId,designation,password,role,(Integer)session.getAttribute("userId"));
        if(created){
            response.sendRedirect("AdminServlet?action=create&role="+role+"&success=true");
        }else{
            response.sendRedirect("AdminServlet?action=create&role="+role+"&error=true");
        }
    }
}