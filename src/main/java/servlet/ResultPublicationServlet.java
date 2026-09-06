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
@WebServlet("/ResultPublicationServlet")
public class ResultPublicationServlet extends HttpServlet{
private static final long serialVersionUID=1L;
protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
HttpSession session=request.getSession(false);
if(session==null||!"EXAM_CELL".equals(session.getAttribute("role"))){
response.sendRedirect("login.jsp");
return;
}
ExamCellAnalysisService service=new ExamCellAnalysisService();
List<Map<String,Object>> departments=service.getApprovedDepartments();
request.setAttribute("departments",departments);
request.getRequestDispatcher("resultPublication.jsp").forward(request,response);
}
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
boolean published=service.publishDepartmentResults(departmentId,userId,remarks);
if(published){
response.sendRedirect("ResultPublicationServlet?published=true");
}else{
response.sendRedirect("ResultPublicationServlet?error=true");
}
}catch(Exception e){
e.printStackTrace();
response.sendRedirect("ResultPublicationServlet?error=true");
}
}
}