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

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,IOException{

        HttpSession session=request.getSession(false);

        if(session==null||!"ADMIN".equals(session.getAttribute("role"))){
            response.sendRedirect("adminLogin.jsp");
            return;
        }

        int page=1;
        int pageSize=20;

        try{
            String pageParameter=request.getParameter("page");
            String pageSizeParameter=request.getParameter("pageSize");

            if(pageParameter!=null){
                page=Integer.parseInt(pageParameter);
            }

            if(pageSizeParameter!=null){
                pageSize=Integer.parseInt(pageSizeParameter);
            }
        }catch(NumberFormatException e){
            page=1;
            pageSize=20;
        }

        if(page<1){
            page=1;
        }

        if(pageSize!=10&&pageSize!=20&&pageSize!=50){
            pageSize=20;
        }

        int totalRecords=service.getManagedAccountsCount();

        int totalPages=(int)Math.ceil(
                (double)totalRecords/pageSize);

        if(totalPages==0){
            totalPages=1;
        }

        if(page>totalPages){
            page=totalPages;
        }

        List<Map<String,Object>> accounts=
                service.getManagedAccounts(page,pageSize);

        request.setAttribute("accounts",accounts);
        request.setAttribute("currentPage",page);
        request.setAttribute("pageSize",pageSize);
        request.setAttribute("totalRecords",totalRecords);
        request.setAttribute("totalPages",totalPages);

        String success=request.getParameter("success");
        String error=request.getParameter("error");

        if("deactivated".equals(success)){
            request.setAttribute(
                    "message",
                    "Account deactivated successfully.");
            request.setAttribute("messageType","success");

        }else if("invalidAccount".equals(error)){
            request.setAttribute(
                    "message",
                    "Invalid account selected.");
            request.setAttribute("messageType","error");

        }else if("alreadyInactive".equals(error)){
            request.setAttribute(
                    "message",
                    "This account is already inactive.");
            request.setAttribute("messageType","error");

        }else if("noTwoFactor".equals(error)){
            request.setAttribute(
                    "message",
                    "This account does not have valid "
                    +"two-factor authentication configured.");
            request.setAttribute("messageType","error");

        }else if("deactivationFailed".equals(error)){
            request.setAttribute(
                    "message",
                    "Account deactivation failed.");
            request.setAttribute("messageType","error");
        }

        request.getRequestDispatcher(
                "manageAccounts.jsp").forward(request,response);
    }
}