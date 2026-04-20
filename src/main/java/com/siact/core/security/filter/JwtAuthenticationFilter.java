package com.siact.core.security.filter;

import com.siact.common.context.LoginContext;
import com.siact.common.utils.JwtUtil;
import com.siact.module.system.dto.LoginUser;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 优先尝试 Authorization header 中的 JWT
        String token = resolveToken(request);
        if (token != null) {
            processJwtToken(token, request, response, filterChain);
            return;
        }

        // 兜底：尝试 query 参数中的下载 token
        String downloadToken = request.getParameter("token");
        if (StringUtils.hasText(downloadToken)) {
            LoginUser loginUser = jwtUtil.consumeDownloadToken(downloadToken);
            if (loginUser != null) {
                setAuthentication(request, loginUser);
                log.debug("Download token authenticated: {}", loginUser.getAccount());
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            clearContext();
        }
    }

    private void processJwtToken(String token, HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        try {
            if (jwtUtil.isTokenValid(token)) {
                setAuthentication(request, jwtUtil.parseToken(token));
            } else {
                String newToken = jwtUtil.refreshToken(token);
                if (newToken != null) {
                    setAuthentication(request, jwtUtil.parseToken(newToken));
                    response.setHeader("X-New-Token", newToken);
                }
            }
        } catch (ExpiredJwtException e) {
            log.debug("JWT expired, refresh failed: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature");
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT");
        } catch (JwtException e) {
            log.warn("JWT error: {}", e.getMessage());
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            clearContext();
        }
    }

    private void setAuthentication(HttpServletRequest request, LoginUser loginUser) {
        LoginContext.setUser(loginUser);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void clearContext() {
        LoginContext.clear();
        SecurityContextHolder.clearContext();
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
