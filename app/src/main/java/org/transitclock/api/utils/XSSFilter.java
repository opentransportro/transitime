/* (C)2023 */
package org.transitclock.api.utils;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class XSSFilter implements Filter {

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
