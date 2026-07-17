package com.siact.hydrocore.common.exception;

import com.siact.hydrocore.common.api.ApiResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void ioExceptionUsesCanonicalEnvelope() {
        ApiResponse<String> response = handler.handleIOException(new IOException("disk failed"), null);

        assertThat(response.getSuccess()).isFalse();
        assertThat(response.getCode()).isEqualTo(500);
        assertThat(response.getMessage()).isEqualTo("IO 异常");
        assertThat(response.getTraceId()).isNotNull();
    }

    @Test
    void authenticationExceptionUsesUnauthorizedCode() {
        ApiResponse<String> response = handler.handleAuthenticationException(
                new org.springframework.security.authentication.BadCredentialsException("bad"));

        assertThat(response.getSuccess()).isFalse();
        assertThat(response.getCode()).isEqualTo(401);
        assertThat(response.getMessage()).isEqualTo("未登录或token已过期");
    }
}
