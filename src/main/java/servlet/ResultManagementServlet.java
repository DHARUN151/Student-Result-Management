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
import service.ResultService;
@WebServlet("/ResultManagementServlet")
public class ResultManagementServlet extends HttpServlet{
    protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        HttpSession session=request.getSession(false);
        if(session==null||!"FACULTY".equals(session.getAttribute("role"))){
            response.sendRedirect("login.jsp");
            return;
        }
        int userId=(Integer)session.getAttribute("userId");
        ResultService service=new ResultService();
        List<Map<String,Object>> drafts=service.getDraftResults(userId);
        request.setAttribute("drafts",drafts);
        request.getRequestDispatcher("resultManagement.jsp").forward(request,response);
    }
    protected void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        HttpSession session=request.getSession(false);
        if(session==null||!"FACULTY".equals(session.getAttribute("role"))){
            response.sendRedirect("login.jsp");
            return;
        }
        try{
            int userId=(Integer)session.getAttribute("userId");
            String action=request.getParameter("action");
            if(!"submit".equals(action)){
                response.sendRedirect("ResultManagementServlet?error=true");
                return;
            }
            int resultId=Integer.parseInt(request.getParameter("resultId"));
            String remarks=request.getParameter("remarks");
            ResultService service=new ResultService();
            boolean success=service.submitResult(resultId,userId,remarks);
            if(success){
                response.sendRedirect("ResultManagementServlet?success=submit");
            }else{
                response.sendRedirect("ResultManagementServlet?error=true");
            }
        }catch(Exception e){
            System.out.println(e);
            response.sendRedirect("ResultManagementServlet?error=true");
        }
    }
}