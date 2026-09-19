package servlet;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import service.LoginService;
import dao.StudentDAO;
@WebServlet("/login")
public class LoginServlet extends HttpServlet{
    private LoginService loginService;
    @Override
    public void init() throws ServletException{
        loginService=new LoginService();
    }
    @Override
    protected void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        String username=request.getParameter("username");
        String password=request.getParameter("password");
        User user=loginService.login(username,password);
        if(user==null){
            response.setContentType("text/html");
            response.getWriter().println("<h2>Invalid username or password</h2>");
            response.getWriter().println("<a href='login.jsp'>Try Again</a>");
            return;
        }
        HttpSession oldSession=request.getSession(false);
        if(oldSession!=null){
            oldSession.invalidate();
        }
        HttpSession session=request.getSession(true);
        session.setMaxInactiveInterval(30*60);
        session.setAttribute("userId",user.getUserId());
        session.setAttribute("username",user.getUsername());
        session.setAttribute("role",user.getRole());
        session.setAttribute("studentId",user.getStudentId());
        String role=user.getRole();
        if("STUDENT".equals(role)){
            Integer userId=(Integer)session.getAttribute("userId");

            if(userId==null){
                response.sendRedirect(
                        request.getContextPath()+"/login.jsp");
                return;
            }

            StudentDAO studentDAO=new StudentDAO();

            Integer studentId=
                    studentDAO.getStudentIdByUserId(userId);

            if(studentId==null){
                response.sendRedirect(
                        request.getContextPath()+"/login.jsp?error=student");
                return;
            }

            session.setAttribute("studentId",studentId);

            response.sendRedirect(
                    request.getContextPath()+
                    "/StudentDashboardServlet");

            return;
        }
        if(user.isFirstLogin()){
            session.setAttribute("pendingUserId",user.getUserId());
            session.setAttribute("pendingUsername",user.getUsername());
            if(user.getTwoFactorSecret()==null||user.getTwoFactorSecret().isEmpty()){
                response.sendRedirect("resetPassword.jsp");
            }else{
                response.sendRedirect("TwoFactorSetupServlet");
            }
            return;
        }
        if(user.isTwoFactorEnabled()){
            session.setAttribute("pendingUserId",user.getUserId());
            response.sendRedirect("TwoFactorLoginServlet");
            return;
        }
        if("FACULTY".equals(role)){
            response.sendRedirect("FacultyDashboardServlet");
        }else if("HOD".equals(role)){
            response.sendRedirect("hodDashboard.jsp");
        }else if("EXAM_CELL".equals(role)){
            response.sendRedirect("ExamCellDashboardServlet");
        }else if("ADMIN".equals(role)){
            response.sendRedirect("adminDashboard.jsp");
        }else{
            response.sendRedirect("login.jsp");
        }
    }
}