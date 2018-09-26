package io.stardog.starwizard.util;

import javax.servlet.http.HttpServletRequest;

public class RequestUtil {
    /**
     * Return a remote IP address, optionally picking up the X-Forwarded-For header used by AWS ELB
     * @param request   request
     * @return  the ip address, from either X-Forwarded-For or the direct connection
     */
    public static String getRemoteIp(HttpServletRequest request) {
        if (request == null) {
            return "127.0.0.1";
        }

        String ip = request.getHeader("X-Forwarded-For") != null ? request.getHeader("X-Forwarded-For") : request.getRemoteAddr();
        if (ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(","));
        }
        return ip;
    }
}
