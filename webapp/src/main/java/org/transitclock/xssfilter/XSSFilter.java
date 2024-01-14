/* (C)2023 */
package org.transitclock.xssfilter;

import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class XSSFilter implements Filter {

    @Override
    public void init(FilterConfig arg0) throws ServletException {}

    @Override
    public void destroy() {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        ServletRequest wrappedRequest = new HttpServletRequestWrapper((HttpServletRequest) request) {
            @Override
            public String getParameter(String param) {
                String str = super.getParameter(param);
                return StringEscapeUtils.escapeHtml4(str);
            }

            @Override
            public Map<String, String[]> getParameterMap() {
                Map<String, String[]> orig = super.getParameterMap();

                Map<String, String[]> map = new HashMap<>();

                for (Entry<String, String[]> entry : orig.entrySet()) {
                    String[] src = entry.getValue();
                    String[] value = new String[src.length];
                    for (int i = 0; i < src.length; i++)
                        value[i] = StringEscapeUtils.escapeHtml4(src[i]);

                    map.put(entry.getKey(), value);
                }

                return map;
            }
        };

        chain.doFilter(wrappedRequest, response);
    }
}
