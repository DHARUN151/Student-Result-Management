package servlet;
import java.io.IOException;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.ExamCellAnalysisService;
@WebServlet("/ExamCellAnalysisServlet")
public class ExamCellAnalysisServlet extends HttpServlet{
private static final long serialVersionUID=1L;
protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
HttpSession session=request.getSession(false);
if(session==null||!"EXAM_CELL".equals(session.getAttribute("role"))){
response.sendRedirect("login.jsp");
return;
}
try{
int departmentId=Integer.parseInt(request.getParameter("departmentId"));
ExamCellAnalysisService service=new ExamCellAnalysisService();
Map<String,Object> analysis=service.getAnalysis(departmentId);
request.setAttribute("analysis",analysis);
request.getRequestDispatcher("examCellAnalysis.jsp").forward(request,response);
}catch(Exception e){
e.printStackTrace();
response.sendRedirect("ExamCellDashboardServlet");
}
}
}