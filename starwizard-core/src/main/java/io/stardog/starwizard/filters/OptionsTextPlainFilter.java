package io.stardog.starwizard.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * iOS Chrome sends an Accept: * / *,image/webp header which causes Dropwizard/Jetty to return OPTIONS requests as
 * Content-type: image/webp. Unfortunately the empty image/webp causes iOS Chrome to break while fetching preflight
 * requests for CORS.
 *
 * As a workaround, just force the use of text/plain for all OPTIONS requests.
 */
public class OptionsTextPlainFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && ((HttpServletRequest)request).getMethod().equals("OPTIONS")) {
            response.setContentType("text/plain");
        } else {
            filterChain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }
}
