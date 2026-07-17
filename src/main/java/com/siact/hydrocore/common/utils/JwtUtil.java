package com.siact.hydrocore.common.utils;

import com.siact.hydrocore.module.system.dto.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-window}")
    private long refreshWindow;

    @Value("${jwt.stale-ttl}")
    private long staleTtl;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String TOKEN_PREFIX = "token:";
    private static final String REFRESH_PREFIX = "token:refresh:";
    private static final String STALE_PREFIX = "token:stale:";
    private static final String LOCK_PREFIX = "token:lock:";
    private static final String DOWNLOAD_PREFIX = "download:";

    /**
     * 生成 token 并存入 Redis
     */
    public String generateToken(LoginUser loginUser) {
        String sessionId = UUID.randomUUID().toString().replaceAll("-", "");
        String token = Jwts.builder()
                .setSubject(loginUser.getAccount())
                .claim("userId", loginUser.getId())
                .claim("sessionId", sessionId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        // 存 token 本体: token:{tokenValue} → userId
        redisTemplate.opsForValue().set(
                TOKEN_PREFIX + token,
                String.valueOf(loginUser.getId()),
                expiration, TimeUnit.MILLISECONDS
        );

        // 存刷新窗口: token:refresh:{userId}:{sessionId} → tokenValue
        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + loginUser.getId() + ":" + sessionId,
                token,
                refreshWindow, TimeUnit.MILLISECONDS
        );

        return token;
    }

    /**
     * 从 token 中解析用户信息
     */
    public LoginUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();

        return toLoginUser(claims);
    }

    /**
     * 从 token 中解析用户信息（允许过期 token）
     */
    public LoginUser parseTokenAllowExpired(String token) {
        try {
            return parseToken(token);
        } catch (ExpiredJwtException e) {
            return toLoginUser(e.getClaims());
        }
    }

    private LoginUser toLoginUser(Claims claims) {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(claims.get("userId", Long.class));
        loginUser.setAccount(claims.getSubject());
        return loginUser;
    }

    /**
     * 获取 token 中的 sessionId
     */
    public String getSessionId(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        return claims.get("sessionId", String.class);
    }

    /**
     * 检查 token 是否在 Redis 中有效（未过期/未被删除）
     */
    public boolean isTokenValid(String token) {
        Boolean hasKey = redisTemplate.hasKey(TOKEN_PREFIX + token);
        return Boolean.TRUE.equals(hasKey);
    }

    /**
     * 尝试刷新过期 token，返回新 token（null 表示刷新窗口已过期）
     */
    public String refreshToken(String oldToken) {
        // 1. 检查 stale 缓存（并发场景：其他请求已完成刷新）
        String staleNewToken = redisTemplate.opsForValue().get(STALE_PREFIX + oldToken);
        if (staleNewToken != null) {
            return staleNewToken;
        }

        // 2. 解析旧 token 获取用户信息（过期 token 需从 ExpiredJwtException 提取 Claims）
        LoginUser loginUser;
        String oldSessionId;
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(oldToken);
            return null; // 未过期，不应走到这里（由 isTokenValid 拦截）
        } catch (ExpiredJwtException e) {
            Claims claims = e.getClaims();
            loginUser = new LoginUser();
            loginUser.setId(claims.get("userId", Long.class));
            loginUser.setAccount(claims.getSubject());
            oldSessionId = claims.get("sessionId", String.class);
        } catch (Exception e) {
            return null;
        }

        // 3. 检查刷新窗口是否存在
        String refreshKey = REFRESH_PREFIX + loginUser.getId() + ":" + oldSessionId;
        Boolean refreshExists = redisTemplate.hasKey(refreshKey);
        if (!Boolean.TRUE.equals(refreshExists)) {
            return null;
        }

        // 4. SETNX 加锁，防止并发刷新
        String lockKey = LOCK_PREFIX + oldToken;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(locked)) {
            try {
                // 5. 生成新 token
                String newToken = generateToken(loginUser);

                // 6. 旧 token 写入 stale 缓存（TTL 短窗口），供并发请求直接获取
                redisTemplate.opsForValue().set(
                        STALE_PREFIX + oldToken,
                        newToken,
                        staleTtl, TimeUnit.MILLISECONDS
                );

                // 7. 删除旧刷新窗口（已消费）
                redisTemplate.delete(refreshKey);

                return newToken;
            } finally {
                redisTemplate.delete(lockKey);
            }
        }

        // 8. 未获取锁，等待并检查 stale 缓存
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
            String cachedNewToken = redisTemplate.opsForValue().get(STALE_PREFIX + oldToken);
            if (cachedNewToken != null) {
                return cachedNewToken;
            }
        }

        return null;
    }

    /**
     * 删除 token（登出时调用）
     */
    public void deleteToken(String token) {
        redisTemplate.delete(TOKEN_PREFIX + token);
    }

    /**
     * 删除用户所有刷新窗口（登出/修改密码时调用）
     */
    public void deleteRefreshTokens(Long userId) {
        Set<String> keys = redisTemplate.keys(REFRESH_PREFIX + userId + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 生成一次性下载 token，存入 Redis（短 TTL，一次性消费）
     */
    public String generateDownloadToken(LoginUser loginUser) {
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        String value = loginUser.getId() + ":" + loginUser.getAccount() + ":" + loginUser.getUsername();
        redisTemplate.opsForValue().set(DOWNLOAD_PREFIX + token, value, 30, TimeUnit.SECONDS);
        return token;
    }

    /**
     * 校验并消费下载 token，返回用户信息；token 无效或已过期返回 null
     */
    public LoginUser consumeDownloadToken(String token) {
        String key = DOWNLOAD_PREFIX + token;
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        redisTemplate.delete(key);
        String[] parts = value.split(":", 3);
        LoginUser loginUser = new LoginUser();
        loginUser.setId(Long.parseLong(parts[0]));
        loginUser.setAccount(parts[1]);
        loginUser.setUsername(parts.length > 2 ? parts[2] : "");
        return loginUser;
    }
}
