package com.siact.hydrocore.core.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siact.hydrocore.common.api.ApiResponse;
import com.siact.hydrocore.common.api.ApiResponseCode;
import com.siact.hydrocore.common.web.TraceIdResolver;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        ApiResponse<Void> result = ApiResponse.success(
                null,
                ApiResponseCode.SUCCESS.getMessage(),
                TraceIdResolver.currentTraceId());
        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
    }
}
