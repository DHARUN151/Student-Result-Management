package servlet;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Subject;
import service.SubjectService;
@WebServlet("/addSubject")
public class AddSubjectServlet extends HttpServlet{
    private static final long serialVersionUID=1L;
    private SubjectService subjectService;
    @Override
    public void init() throws ServletException{
        subjectService=new SubjectService();
    }
    @Override
    protected void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        HttpSession session=request.getSession(false);
        if(session==null||!"FACULTY".equals(session.getAttribute("role"))){
            response.sendRedirect("login.jsp");
            return;
        }
        try{
            int userId=(Integer)session.getAttribute("userId");
            String subCode=request.getParameter("subCode");
            String subName=request.getParameter("subName");
            String semester=request.getParameter("semester");
            String credits=request.getParameter("credits");
            Subject subject=new Subject();
            subject.setSubCode(subCode);
            subject.setSubName(subName);
            subject.setSemester(Integer.parseInt(semester));
            subject.setCredits(Integer.parseInt(credits));
            boolean success=subjectService.addSubject(subject,userId);
            if(success){
                response.sendRedirect("addSubject.jsp?success=true");
            }else{
                response.sendRedirect("addSubject.jsp?error=true");
            }
        }catch(Exception e){
            System.out.println("Error while adding subject");
            System.out.println(e.getMessage());
            response.sendRedirect("addSubject.jsp?error=true");
        }
    }
}