package com.siact.hydrocore.core.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siact.hydrocore.common.api.ApiResponse;
import com.siact.hydrocore.common.api.ApiResponseCode;
import com.siact.hydrocore.common.web.TraceIdResolver;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        ApiResponse<Void> result = ApiResponse.fail(
                ApiResponseCode.UNAUTHORIZED.getCode(),
                ApiResponseCode.UNAUTHORIZED.getMessage(),
                TraceIdResolver.currentTraceId());
        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
    }
}
