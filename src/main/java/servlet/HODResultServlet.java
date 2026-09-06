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
import service.HODResultService;
import service.FacultySubjectService;
@WebServlet("/HODResultServlet")
public class HODResultServlet extends HttpServlet{
    private static final long serialVersionUID=1L;
    private HODResultService service=new HODResultService();
    protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        HttpSession session=request.getSession(false);
        if(session==null||!"HOD".equals(session.getAttribute("role"))){
            response.sendRedirect("login.jsp");
            return;
        }
        int userId=(Integer)session.getAttribute("userId");
        FacultySubjectService facultySubjectService=new FacultySubjectService();
        List<Map<String,Object>> departmentFaculty=facultySubjectService.getDepartmentFaculty(userId);
        List<Map<String,Object>> departmentOfferings=facultySubjectService.getDepartmentOfferings(userId);
        request.setAttribute("departmentFaculty",departmentFaculty);
        request.setAttribute("departmentOfferings",departmentOfferings);
        Map<String,Object> hodDetails=service.getHODDetails(userId);
        Map<String,Integer> departmentSummary=service.getDepartmentSummary(userId);
        List<Map<String,Object>> faculty=service.getDepartmentFaculty(userId);
        List<Map<String,Object>> facultySubjects=service.getFacultySubjects(userId);
        Map<String,Integer> resultStatus=service.getResultStatusSummary(userId);
        Map<String,Object> performance=service.getDepartmentPerformance(userId);
        List<Map<String,Object>> submittedResults=service.getSubmittedResults(userId);
        request.setAttribute("hodDetails",hodDetails);
        request.setAttribute("departmentSummary",departmentSummary);
        request.setAttribute("faculty",faculty);
        request.setAttribute("facultySubjects",facultySubjects);
        request.setAttribute("resultStatus",resultStatus);
        request.setAttribute("performance",performance);
        request.setAttribute("submittedResults",submittedResults);
        request.getRequestDispatcher("hodDashboard.jsp").forward(request,response);
        
    }
    protected void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        HttpSession session=request.getSession(false);
        if(session==null||!"HOD".equals(session.getAttribute("role"))){
            response.sendRedirect("login.jsp");
            return;
        }
        int userId=(Integer)session.getAttribute("userId");
        String action=request.getParameter("action");
        String resultIdParameter=request.getParameter("resultId");
        String remarks=request.getParameter("remarks");
        if(resultIdParameter==null){
            response.sendRedirect("HODResultServlet");
            return;
        }
        try{
            int resultId=Integer.parseInt(resultIdParameter);
            boolean success=false;
            if("verify".equals(action)){
                success=service.verifyResult(resultId,userId,remarks);
            }else if("reject".equals(action)){
                success=service.rejectResult(resultId,userId,remarks);
            }
            if(success){
                response.sendRedirect("HODResultServlet?success=true");
            }else{
                response.sendRedirect("HODResultServlet?error=true");
            }
        }catch(NumberFormatException e){
            response.sendRedirect("HODResultServlet?error=true");
        }
    }
}