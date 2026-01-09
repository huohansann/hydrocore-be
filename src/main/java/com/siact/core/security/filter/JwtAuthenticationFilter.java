package com.siact.core.security.filter;


import com.alibaba.fastjson2.JSON;
import com.siact.common.utils.JwtUtil;
import com.siact.common.utils.LoginUntil;
import com.siact.module.permission.dto.UserTokenDTO;
import io.jsonwebtoken.JwtException;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 处理JWT认证过滤器逻辑
     * 1. 从请求中解析JWT令牌
     * 2. 验证令牌有效性
     * 3. 设置认证信息到Security上下文
     *
     * @param request     HTTP请求对象，包含客户端请求信息
     * @param response    HTTP响应对象，用于构建服务器响应
     * @param filterChain 过滤器链，用于继续执行后续过滤器
     * @throws ServletException 当servlet处理发生异常时抛出
     * @throws IOException      当I/O操作发生异常时抛出
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 获取请求路径
            String requestUri = request.getRequestURI();

            // 定义豁免路径列表
            List<String> excludedPaths = Arrays.asList(
                    "/kiln-control/**/auth/login",
                    "/kiln-control/**/auth/modifyPwd",
                    "/kiln-control/**/doc.html",
                    // swagger相关资源文件
                    "/kiln-control/**/webjars/**",
                    "/kiln-control/**/swagger-resources/**",
                    "/kiln-control/**/v2/api-docs",
                    "/kiln-control/**/favicon.ico",
                    "/kiln-control/**/algorithm/*",
                    "/kiln-control/ws"
            );

            // 检查请求路径是否在豁免列表中
            AntPathMatcher pathMatcher = new AntPathMatcher();
            boolean isExcludedPath = excludedPaths.stream()
                    .anyMatch(pattern -> pathMatcher.match(pattern, requestUri));

            if (isExcludedPath) {
                // 如果是豁免路径，直接放行
                filterChain.doFilter(request, response);
                return;
            }
            String token = resolveToken(request);

            // 如果 Token 不存在或验证失败，返回 401 Unauthorized
            if (token == null || !validateToken(token)) {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"code\":401,\"message\":\"未授权或 Token 失效\"}");
                return;
            }

            // Token 存在且有效，进行后续认证逻辑
            UserTokenDTO curUser = jwtUtil.extractUsername(token);
            LoginUntil.setCurrentUser(JSON.toJSONString(curUser));

            if (ObjectUtils.isEmpty(curUser)) {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"code\":401,\"message\":\"未授权或 Token 失效\"}");
                return;
            }
            // ---------- 单点登录  根据用户账号查询
            // String account = curUser.getAccount();
            // String redisToken = redisTemplate.opsForValue().get(account);

            // ---------- 多点登录 根据token查询
            String redisToken = redisTemplate.opsForValue().get(token);

            if (ObjectUtils.isEmpty(redisToken) || (ObjectUtils.isNotEmpty(redisToken) && !redisToken.equals(token))) {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"code\":401,\"message\":\"未授权或 Token 失效\"}");
                return;
            }

//            if (redisToken != null && redisToken.equals(token)) {
//                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username
//                        , null, new ArrayList<>());
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//            }

            // 继续过滤链
            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            // 捕获 JwtException 异常，返回 401 Unauthorized
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":401,\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * 从HTTP请求头中解析Bearer令牌
     *
     * @param request HTTP请求对象，用于获取Authorization请求头
     * @return String 成功时返回去除"Bearer "前缀的令牌字符串，未找到有效令牌时返回null
     * <p>
     * 处理逻辑：
     * 1. 从Authorization请求头获取原始字符串
     * 2. 验证字符串是否符合Bearer令牌格式（非空且以"Bearer "开头）
     * 3. 返回去除前缀的令牌（保留7位之后的字符）
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // 验证并处理Authorization头格式
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }


    /**
     * 验证JWT令牌的有效性
     *
     * @param token 需要验证的JWT令牌字符串
     * @return boolean 验证结果：
     * true - 令牌有效（能成功解析出用户名）
     * false - 令牌无效（解析过程中发生异常）
     * <p>
     * 实现逻辑：
     * 通过调用JWT工具类解析用户名来间接验证令牌有效性。
     * 若解析过程抛出任何异常（如过期/篡改/格式错误），则视为无效令牌
     */
    private boolean validateToken(String token) {
        try {
            // 核心验证逻辑：尝试解析令牌中的用户信息
            // 成功解析则返回true，异常情况由catch处理
            jwtUtil.extractUsername(token);
            return true;
        } catch (Exception e) {
            // 捕获所有解析异常（包括过期、签名错误、格式错误等）
            // 统一返回无效令牌标识
            return false;
        }
    }

}
