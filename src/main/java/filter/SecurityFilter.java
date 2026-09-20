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
import service.AuthorizationService;

@WebFilter("/*")
public class SecurityFilter implements Filter {
    private AuthorizationService authorizationService=new AuthorizationService();

    @Override
    public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain)throws IOException,ServletException {
        HttpServletRequest httpRequest=(HttpServletRequest)request;
        HttpServletResponse httpResponse=(HttpServletResponse)response;
        String path=httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        if(path.equals("/adminLogin.jsp")||path.equals("/adminLogin")||path.equals("/login")||path.equals("/login.jsp")||path.startsWith("/css/")||path.startsWith("/images/")||path.startsWith("/js/")|| path.equals("/saml/idp/metadata")|| path.equals("/saml/sp/metadata")|| path.equals("/saml/login")|| path.equals("/saml/idp/sso")|| path.equals("/saml/idp/sso")
        		|| path.equals("/saml/idp/authenticate")
        		|| path.equals("/saml/idp/2fa")
        		|| path.equals("/samlIdpLogin.jsp")
        		|| path.equals("/samlIdpTwoFactor.jsp")|| path.equals("/saml/acs")
        		|| path.equals("/saml/idp/sso")
        		|| path.equals("/saml/idp/2fa")) {
            chain.doFilter(request,response);
            return;
        }

        HttpSession session=httpRequest.getSession(false);

        if(session==null||session.getAttribute("userId")==null) {
            if(path.equals("/AdminServlet")||path.startsWith("/AdminServlet/")||path.equals("/adminDashboard.jsp")||path.equals("/ManageAccountsServlet")||path.equals("/AccountDeactivationServlet")||path.equals("/VerifyAccountDeactivationServlet")) {
                httpResponse.sendRedirect(httpRequest.getContextPath()+"/adminLogin.jsp");
            } else {
                httpResponse.sendRedirect(httpRequest.getContextPath()+"/login.jsp");
            }
            return;
        }

        int userId=(Integer)session.getAttribute("userId");
        String role=(String)session.getAttribute("role");

        httpResponse.setHeader("Cache-Control","no-cache,no-store,must-revalidate");
        httpResponse.setHeader("Pragma","no-cache");
        httpResponse.setDateHeader("Expires",0);

        if(path.equals("/adminDashboard.jsp")||path.equals("/AdminServlet")) {
            if(!"ADMIN".equals(role)&&!"SUPER_ADMIN".equals(role)) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,"Access Denied");
                return;
            }
        }

        if(path.equals("/ManageAccountsServlet")) {
            if(!authorizationService.hasPermission(userId,"USER_VIEW")) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,"Access Denied");
                return;
            }
        }

        if(path.equals("/AccountDeactivationServlet")||path.equals("/VerifyAccountDeactivationServlet")) {
            if(!authorizationService.hasPermission(userId,"USER_DEACTIVATE")) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,"Access Denied");
                return;
            }
        }

        if(path.equals("/teacherDashboard.jsp")) {
            if(!"FACULTY".equals(role)) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,"Access Denied");
                return;
            }
        }

        if((path.equals("/studentDashboard.jsp")||
        	    path.equals("/StudentDashboardServlet"))&&
        	   !"STUDENT".equals(role)) {

        	    httpResponse.sendError(
        	            HttpServletResponse.SC_FORBIDDEN,
        	            "Access Denied");

        	    return;
        	}

        if(path.equals("/hodDashboard.jsp")) {
            if(!"HOD".equals(role)) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,"Access Denied");
                return;
            }
        }

        if(path.equals("/ExamCellDashboardServlet")) {
            if(!"EXAM_CELL".equals(role)) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,"Access Denied");
                return;
            }
        }

        chain.doFilter(request,response);
    }
}