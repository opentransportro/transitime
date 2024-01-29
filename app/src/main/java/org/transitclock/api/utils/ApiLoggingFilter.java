/* (C)2023 */
package org.transitclock.api.utils;

import jakarta.servlet.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiLoggingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfg) {
        logger.info("ApiLoggingFilter init");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) {
        try {
            filterChain.doFilter(request, response);
        } catch (Throwable ex) {
            logger.error("Filter caught exception: ", ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void destroy() {
        logger.info("ApiLoggingFilter destroy");
    }
}
