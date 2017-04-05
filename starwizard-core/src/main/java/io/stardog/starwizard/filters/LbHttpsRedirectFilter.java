package io.stardog.starwizard.filters;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This filter forces a redirect to the HTTPS version of an HTTP-prefixed URL, if
 * the X-Forwarded-Proto header is set to "http". This is the case for Amazon's
 * Elastic Load Balancer service when forwarding HTTP requests.
 */
public class LbHttpsRedirectFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse
                && "http".equals(((HttpServletRequest)request).getHeader("X-Forwarded-Proto"))) {
            String url = ((HttpServletRequest)request).getRequestURL().toString();
            HttpServletResponse httpResponse = (HttpServletResponse)response;
            httpResponse.sendRedirect(url.replace("http://", "https://"));
        } else {
            filterChain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }
}
