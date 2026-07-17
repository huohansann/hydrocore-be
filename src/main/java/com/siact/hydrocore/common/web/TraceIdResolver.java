package com.siact.hydrocore.common.web;

import org.slf4j.MDC;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public final class TraceIdResolver {
    private static final String TRACE_ID = "traceId";
    private static final String TRACE_HEADER = "X-Trace-Id";

    private TraceIdResolver() {
    }

    public static String currentTraceId() {
        String fromMdc = trimToNull(MDC.get(TRACE_ID));
        if (fromMdc != null) {
            return fromMdc;
        }
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
            String fromHeader = trimToNull(request.getHeader(TRACE_HEADER));
            if (fromHeader != null) {
                return fromHeader;
            }
        }
        return "";
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
