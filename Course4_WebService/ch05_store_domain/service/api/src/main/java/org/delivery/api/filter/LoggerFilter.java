package org.delivery.api.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

@Slf4j
@Component
public class LoggerFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper((HttpServletRequest) request);
        ContentCachingResponseWrapper res = new ContentCachingResponseWrapper((HttpServletResponse) response);
        log.info("INIT URI: {}", req.getRequestURI());

        chain.doFilter(req, res);

        // request info
        Enumeration<String> headerNames = req.getHeaderNames();
        StringBuilder requestHeaderValues = new StringBuilder();
        headerNames.asIterator().forEachRemaining(headerKey -> {
            requestHeaderValues
                    .append("[")
                    .append(headerKey)
                    .append(" : ")
                    .append(req.getHeader(headerKey))
                    .append("] ");
        });
        String requestBody = new String(req.getContentAsByteArray());
        String uri = req.getRequestURI();
        String method = req.getMethod();

        log.info(">>>>> uri : {}, method : {}, header : {}, body : {}", uri, method, requestHeaderValues, requestBody);

        // response info
        StringBuilder responseHeaderValues = new StringBuilder();
        res.getHeaderNames().forEach(headerKey -> {
            responseHeaderValues
                    .append("[")
                    .append(headerKey)
                    .append(" : ")
                    .append(res.getHeader(headerKey) + "] ");
        });
        String responseBody = new String(res.getContentAsByteArray());
        log.info("<<<<< uri : {}, method : {}, header : {}, body : {}", uri, method, responseHeaderValues, responseBody);

        res.copyBodyToResponse();
    }
}
