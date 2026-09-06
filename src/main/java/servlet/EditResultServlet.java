package servlet;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.MarksService;
@WebServlet("/EditResultServlet")
public class EditResultServlet extends HttpServlet {
private static final long serialVersionUID=1L;
protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
HttpSession session=request.getSession(false);
if(session==null||!"FACULTY".equals(session.getAttribute("role"))){
response.sendRedirect("login.jsp");
return;
}
try{
int resultId=Integer.parseInt(request.getParameter("resultId"));
int userId=(Integer)session.getAttribute("userId");
MarksService service=new MarksService();
request.setAttribute("resultId",resultId);
request.setAttribute("marks",service.getResultMarks(resultId,userId));
request.getRequestDispatcher("editResult.jsp").forward(request,response);
}catch(Exception e){
e.printStackTrace();
response.sendRedirect("ResultManagementServlet?error=true");
}
}
protected void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
HttpSession session=request.getSession(false);
if(session==null||!"FACULTY".equals(session.getAttribute("role"))){
response.sendRedirect("login.jsp");
return;
}
try{
int resultId=Integer.parseInt(request.getParameter("resultId"));
int markId=Integer.parseInt(request.getParameter("markId"));
int internalMark=Integer.parseInt(request.getParameter("internalMark"));
int externalMark=Integer.parseInt(request.getParameter("externalMark"));
int userId=(Integer)session.getAttribute("userId");
MarksService service=new MarksService();
boolean success=service.updateMarks(markId,internalMark,externalMark,resultId,userId);
if(success){
response.sendRedirect("EditResultServlet?resultId="+resultId+"&success=true");
}else{
response.sendRedirect("EditResultServlet?resultId="+resultId+"&error=true");
}
}catch(Exception e){
e.printStackTrace();
response.sendRedirect("ResultManagementServlet?error=true");
}
}
}