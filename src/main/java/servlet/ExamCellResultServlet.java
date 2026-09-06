package servlet;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.ResultService;
@WebServlet("/ExamCellResultServlet")
public class ExamCellResultServlet extends HttpServlet{
private static final long serialVersionUID=1L;
protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
HttpSession session=request.getSession(false);
if(session==null||!"EXAM_CELL".equals(session.getAttribute("role"))){
response.sendRedirect("login.jsp");
return;
}
ResultService service=new ResultService();
request.setAttribute("results",service.getHODVerifiedResults());
request.getRequestDispatcher("examCellResult.jsp").forward(request,response);
}
protected void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
HttpSession session=request.getSession(false);
if(session==null||!"EXAM_CELL".equals(session.getAttribute("role"))){
response.sendRedirect("login.jsp");
return;
}
try{
int resultId=Integer.parseInt(request.getParameter("resultId"));
int userId=(Integer)session.getAttribute("userId");
String remarks=request.getParameter("remarks");
ResultService service=new ResultService();
boolean success=service.approveResult(resultId,userId,remarks);
if(success){
response.sendRedirect("ExamCellResultServlet?success=true");
}else{
response.sendRedirect("ExamCellResultServlet?error=true");
}
}catch(Exception e){
e.printStackTrace();
response.sendRedirect("ExamCellResultServlet?error=true");
}
}
}