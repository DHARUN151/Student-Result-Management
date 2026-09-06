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
import service.ExamCellAnalysisService;
@WebServlet("/ExamCellDashboardServlet")
public class ExamCellDashboardServlet extends HttpServlet{
private static final long serialVersionUID=1L;
protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
HttpSession session=request.getSession(false);
if(session==null||!"EXAM_CELL".equals(session.getAttribute("role"))){
response.sendRedirect("login.jsp");
return;
}
ExamCellAnalysisService service=new ExamCellAnalysisService();
List<Map<String,Object>> departments=service.getDepartments();
request.setAttribute("departments",departments);
request.getRequestDispatcher("examCellDashboard.jsp").forward(request,response);
}
}