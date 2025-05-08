import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String contextPath = httpRequest.getContextPath(); // ie /cs122b_project1_war
        String requested_resource =
                httpRequest.getRequestURI()
                            .substring(contextPath.length() + 1);
        // ie login.html, _dashboard/login.html

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(requested_resource)) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

        // Redirect to login page if the "user" attribute doesn't exist in session
        if (httpRequest.getSession().getAttribute("user") == null) {
            httpResponse.sendRedirect(contextPath + "/login.html");
        } else if (this.isAdminProtectedUrl(requested_resource) &&
                    httpRequest.getSession().getAttribute("employee") == null
        ) {
            httpResponse.sendRedirect(contextPath + "/_dashboard/login.html");
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::equals);
    }

    private boolean isAdminProtectedUrl(String requestURI) {
        return requestURI.startsWith("_dashboard") &&
                !requestURI.equals("_dashboard/login.html") &&
                !requestURI.equals("_dashboard/login.js") &&
                !requestURI.equals("_dashboard/api/login");
    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add("login.html");
        allowedURIs.add("login.js");
        allowedURIs.add("logout.js");
        allowedURIs.add("api/login");
    }

    public void destroy() {
        // ignored.
    }

}
