/* (C)2023 */
package org.transitclock.utils.web;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(WebLoggingFilter.class);

    @Override
    public void init(FilterConfig filterConfg) {
        logger.info("WebLoggingFilter init");
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
        logger.info("WebLoggingFilter destroy");
    }
}
