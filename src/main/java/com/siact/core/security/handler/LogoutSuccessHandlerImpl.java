package com.siact.core.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siact.common.entity.ResponseEntity;
import com.siact.common.enums.ResponseEnum;
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
        ResponseEntity<Void> result = ResponseEntity.<Void>builder()
                .code(ResponseEnum.SUCCESS.code())
                .message(ResponseEnum.SUCCESS.content())
                .build();
        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
    }
}
