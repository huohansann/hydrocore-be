package com.siact.hydrocore.common.utils;

import com.siact.hydrocore.common.redis.RedisService;
import com.siact.hydrocore.module.system.dto.LoginUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtUtilTest {
    private static final String SECRET = "HydroCoreLocalDevJwtSecret_2026_7f4c9a2e";

    private RedisService redisService;
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        redisService = mock(RedisService.class);
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 60000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshWindow", 120000L);
        ReflectionTestUtils.setField(jwtUtil, "staleTtl", 5000L);
        ReflectionTestUtils.setField(jwtUtil, "redisService", redisService);
    }

    @Test
    void generateTokenStoresTokenAndRefreshWindowWithMillisecondTtl() {
        LoginUser user = loginUser();

        String token = jwtUtil.generateToken(user);

        assertThat(token).isNotBlank();
        verify(redisService).setString(eq("token:" + token), eq("7"), eq(60000L), eq(TimeUnit.MILLISECONDS));
        verify(redisService).setString(
                org.mockito.ArgumentMatchers.startsWith("token:refresh:7:"),
                eq(token),
                eq(120000L),
                eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    void refreshTokenReturnsStaleTokenWhenConcurrentRefreshAlreadyCompleted() {
        when(redisService.getString("token:stale:old")).thenReturn("new-token");

        assertThat(jwtUtil.refreshToken("old")).isEqualTo("new-token");
    }

    @Test
    void refreshTokenUsesSetNxLockAndDeletesConsumedRefreshWindow() {
        String oldToken = expiredToken("session-a");
        when(redisService.getString("token:stale:" + oldToken)).thenReturn(null);
        when(redisService.hasKey("token:refresh:7:session-a")).thenReturn(true);
        when(redisService.tryLock(eq("token:lock:" + oldToken), org.mockito.ArgumentMatchers.anyString(), eq(10L))).thenReturn(true);

        String newToken = jwtUtil.refreshToken(oldToken);

        assertThat(newToken).isNotBlank();
        verify(redisService).setString(eq("token:stale:" + oldToken), eq(newToken), eq(5000L), eq(TimeUnit.MILLISECONDS));
        verify(redisService).delete("token:refresh:7:session-a");

        ArgumentCaptor<String> lockValue = ArgumentCaptor.forClass(String.class);
        verify(redisService).tryLock(eq("token:lock:" + oldToken), lockValue.capture(), eq(10L));
        assertThat(lockValue.getValue()).isNotEqualTo("1");
        verify(redisService).unlock("token:lock:" + oldToken, lockValue.getValue());
    }

    @Test
    void deleteRefreshTokensDeletesAllMatchingKeys() {
        when(redisService.keys("token:refresh:7:*")).thenReturn(Collections.singleton("token:refresh:7:a"));

        jwtUtil.deleteRefreshTokens(7L);

        verify(redisService).deleteAll(Collections.singleton("token:refresh:7:a"));
    }

    @Test
    void consumeDownloadTokenReadsDeletesAndReturnsLoginUser() {
        when(redisService.getString("download:abc")).thenReturn("7:acct:Alice");

        LoginUser user = jwtUtil.consumeDownloadToken("abc");

        assertThat(user.getId()).isEqualTo(7L);
        assertThat(user.getAccount()).isEqualTo("acct");
        assertThat(user.getUsername()).isEqualTo("Alice");
        verify(redisService).delete("download:abc");
    }

    private LoginUser loginUser() {
        LoginUser user = new LoginUser();
        user.setId(7L);
        user.setAccount("acct");
        user.setUsername("Alice");
        return user;
    }

    private String expiredToken(String sessionId) {
        return Jwts.builder()
                .setSubject("acct")
                .claim("userId", 7L)
                .claim("sessionId", sessionId)
                .setIssuedAt(new Date(System.currentTimeMillis() - 120000L))
                .setExpiration(new Date(System.currentTimeMillis() - 60000L))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
