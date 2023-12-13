/* (C)2023 */
package org.transitclock.api.utils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(ApiLoggingFilter.class);

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
