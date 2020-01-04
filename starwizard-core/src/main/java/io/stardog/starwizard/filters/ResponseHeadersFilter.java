package io.stardog.starwizard.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * This filter adds standard response headers to every response; for example, security headers such as
 * Strict-Transport-Security
 */
public class ResponseHeadersFilter implements Filter {
    private final Map<String,String> headers;

    public ResponseHeadersFilter(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = (HttpServletResponse)response;
            for (Map.Entry<String,String> e : headers.entrySet()) {
                httpResponse.addHeader(e.getKey(), e.getValue());
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
