package filter;
import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
@WebFilter("/*")
public class SecurityFilter implements Filter{
    @Override
    public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain)throws IOException,ServletException{
        HttpServletRequest httpRequest=(HttpServletRequest)request;
        HttpServletResponse httpResponse=(HttpServletResponse)response;
        String path=httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        if(path.equals("/adminLogin.jsp")||path.equals("/adminLogin")||path.equals("/login")||path.equals("/login.jsp")||path.startsWith("/css/")||path.startsWith("/images/")){
            chain.doFilter(request,response);
            return;
        }
        HttpSession session=httpRequest.getSession(false);
        String role=session==null?null:(String)session.getAttribute("role");
        if(path.equals("/AdminServlet")||path.startsWith("/AdminServlet/")||path.equals("/adminDashboard.jsp")||path.equals("/ManageAccountsServlet")||path.equals("/AccountDeactivationServlet")||path.equals("/VerifyAccountDeactivationServlet")){
            if(session==null||!"ADMIN".equals(role)){
                httpResponse.sendRedirect(httpRequest.getContextPath()+"/adminLogin.jsp");
                return;
            }
            httpResponse.setHeader("Cache-Control","no-cache,no-store,must-revalidate");
            httpResponse.setHeader("Pragma","no-cache");
            httpResponse.setDateHeader("Expires",0);
            chain.doFilter(request,response);
            return;
        }
        if(session==null||session.getAttribute("userId")==null){
            httpResponse.sendRedirect(httpRequest.getContextPath()+"/login.jsp");
            return;
        }
        httpResponse.setHeader("Cache-Control","no-cache,no-store,must-revalidate");
        httpResponse.setHeader("Pragma","no-cache");
        httpResponse.setDateHeader("Expires",0);
        if(path.equals("/teacherDashboard.jsp")&&!"FACULTY".equals(role)){
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,"Access Denied");
            return;
        }
        if(path.equals("/studentDashboard.jsp")&&!"STUDENT".equals(role)){
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,"Access Denied");
            return;
        }
        if(path.equals("/hodDashboard.jsp")&&!"HOD".equals(role)){
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,"Access Denied");
            return;
        }
        if(path.equals("/ExamCellDashboardServlet")&&!"EXAM_CELL".equals(role)){
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,"Access Denied");
            return;
        }
        chain.doFilter(request,response);
    }
}