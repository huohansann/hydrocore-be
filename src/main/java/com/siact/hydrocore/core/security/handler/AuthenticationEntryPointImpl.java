package com.siact.hydrocore.core.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siact.hydrocore.common.entity.ResponseEntity;
import com.siact.hydrocore.common.enums.ResponseEnum;
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
        ResponseEntity<Void> result = ResponseEntity.<Void>builder()
                .code(ResponseEnum.UNAUTHORIZED.code())
                .message(ResponseEnum.UNAUTHORIZED.content())
                .build();
        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
    }
}
