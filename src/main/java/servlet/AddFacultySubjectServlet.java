package servlet;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.FacultySubjectService;
@WebServlet("/AddFacultySubjectServlet")
public class AddFacultySubjectServlet extends HttpServlet{
    private static final long serialVersionUID=1L;
    private FacultySubjectService service=new FacultySubjectService();
    protected void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        HttpSession session=request.getSession(false);
        if(session==null||!"HOD".equals(session.getAttribute("role"))){
            response.sendRedirect("login.jsp");
            return;
        }
        try{
            int hodUserId=(Integer)session.getAttribute("userId");
            int facultyId=Integer.parseInt(request.getParameter("facultyId"));
            int offeringId=Integer.parseInt(request.getParameter("offeringId"));
            boolean success=service.assignSubject(facultyId,offeringId,hodUserId);
            if(success){
                response.sendRedirect("HODResultServlet?assignmentSuccess=true");
            }else{
                response.sendRedirect("HODResultServlet?assignmentError=true");
            }
        }catch(Exception e){
            System.out.println(e);
            response.sendRedirect("HODResultServlet?assignmentError=true");
        }
    }
}