package io.stardog.starwizard.filters;

import org.junit.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class LbHttpsRedirectFilterTest {
    @Test
    public void doFilter() throws Exception {
        LbHttpsRedirectFilter filter = new LbHttpsRedirectFilter();
        filter.init(null);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getHeader("X-Forwarded-Proto"))
                .thenReturn("http");
        when(request.getRequestURL())
                .thenReturn(new StringBuffer("http://www.example.com"));

        filter.doFilter(request, response, chain);

        verify(response).sendRedirect("https://www.example.com");

        filter.destroy();
    }

    @Test
    public void dontDoFilter() throws Exception {
        LbHttpsRedirectFilter filter = new LbHttpsRedirectFilter();

        ServletRequest request = mock(ServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }

}
