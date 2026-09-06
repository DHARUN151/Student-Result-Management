package servlet;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.ExamCellAnalysisService;
@WebServlet("/ExamCellApprovalServlet")
public class ExamCellApprovalServlet extends HttpServlet{
private static final long serialVersionUID=1L;
protected void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
HttpSession session=request.getSession(false);
if(session==null||!"EXAM_CELL".equals(session.getAttribute("role"))){
response.sendRedirect("login.jsp");
return;
}
try{
int departmentId=Integer.parseInt(request.getParameter("departmentId"));
int userId=(Integer)session.getAttribute("userId");
String remarks=request.getParameter("remarks");
ExamCellAnalysisService service=new ExamCellAnalysisService();
boolean approved=service.approveDepartmentResults(departmentId,userId,remarks);
if(approved){
response.sendRedirect("ExamCellAnalysisServlet?departmentId="+departmentId+"&approved=true");
}else{
response.sendRedirect("ExamCellAnalysisServlet?departmentId="+departmentId+"&error=true");
}
}catch(Exception e){
e.printStackTrace();
response.sendRedirect("ExamCellDashboardServlet");
}
}
}