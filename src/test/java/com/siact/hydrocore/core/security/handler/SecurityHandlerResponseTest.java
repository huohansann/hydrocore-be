package com.siact.hydrocore.core.security.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityHandlerResponseTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void authenticationEntryPointWritesCanonicalEnvelope() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        new AuthenticationEntryPointImpl().commence(
                new MockHttpServletRequest(),
                response,
                new BadCredentialsException("bad"));

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(body.get("success").asBoolean()).isFalse();
        assertThat(body.get("code").asInt()).isEqualTo(401);
        assertThat(body.get("traceId").asText()).isNotNull();
    }

    @Test
    void logoutSuccessWritesCanonicalEnvelope() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        new LogoutSuccessHandlerImpl().onLogoutSuccess(new MockHttpServletRequest(), response, null);

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body.get("success").asBoolean()).isTrue();
        assertThat(body.get("code").asInt()).isEqualTo(200);
        assertThat(body.get("traceId").asText()).isNotNull();
    }
}
