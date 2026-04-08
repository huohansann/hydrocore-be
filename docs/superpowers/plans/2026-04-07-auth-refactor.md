# 认证模块重构实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将登录认证从旧 `sys_user` 表全面迁移到新 `sys_user_new` 表，重写 Token 机制（单 token + 滑动过期 + 并发安全刷新），删除旧 permission 模块。

**Architecture:** 保留 Spring Security 框架，重写 SecurityConfig、JwtAuthenticationFilter、JwtUtil。认证业务代码放入 `module/system`，安全基础设施放在 `core/security`。用 `LoginContext`（类型安全 ThreadLocal）替代旧的 `LoginUntil`（JSON 字符串 ThreadLocal）。

**Tech Stack:** Spring Security, JJWT 0.11.5, Redis, Hutool BCrypt, MyBatis-Plus

---

### Task 1: 创建 LoginContext 和 LoginUser

**Files:**
- Create: `src/main/java/com/siact/common/context/LoginContext.java`
- Create: `src/main/java/com/siact/module/system/dto/LoginUser.java`

- [ ] **Step 1: 创建 LoginUser DTO**

在 `module/system/dto/` 下创建 `LoginUser.java`，替代旧 `permission.dto.UserTokenDTO`。只存认证所需的最小字段（id、account、username），角色/菜单等数据按需从数据库查询。

```java
package com.siact.module.system.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class LoginUser implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String account;
    private String username;
}
```

- [ ] **Step 2: 创建 LoginContext**

在 `common/context/` 下创建 `LoginContext.java`，替代旧 `common/utils/LoginUntil.java`。直接存 `LoginUser` 对象，不做 JSON 序列化。

```java
package com.siact.common.context;

import com.siact.module.system.dto.LoginUser;

public class LoginContext {
    private static final ThreadLocal<LoginUser> USER_HOLDER = new ThreadLocal<>();

    public static void setUser(LoginUser user) {
        USER_HOLDER.set(user);
    }

    public static LoginUser getUser() {
        return USER_HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser user = USER_HOLDER.get();
        return user != null ? user.getId() : null;
    }

    public static String getAccount() {
        LoginUser user = USER_HOLDER.get();
        return user != null ? user.getAccount() : null;
    }

    public static void clear() {
        USER_HOLDER.remove();
    }
}
```

- [ ] **Step 3: 验证编译**

Run: `mvn compile -pl . -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/siact/common/context/LoginContext.java src/main/java/com/siact/module/system/dto/LoginUser.java
git commit -m "feat(auth): 创建 LoginContext 和 LoginUser 基础类型"
```

---

### Task 2: 重写 JwtUtil

**Files:**
- Rewrite: `src/main/java/com/siact/common/utils/JwtUtil.java`

- [ ] **Step 1: 重写 JwtUtil**

实现单 token + 滑动过期 + 并发安全刷新机制。Token claims 只存 userId 和 account（轻量）。Redis 存储两份数据：token 本体（短 TTL）和刷新窗口（长 TTL）。

```java
package com.siact.common.utils;

import com.siact.module.system.dto.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
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

    /**
     * 生成 token 并存入 Redis
     *
     * @param loginUser 登录用户信息
     * @return token 字符串
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
     *
     * @param oldToken 过期的旧 token
     * @return 新 token，如果刷新窗口已过期返回 null
     */
    public String refreshToken(String oldToken) {
        // 1. 检查 stale 缓存（并发场景：其他请求已完成刷新）
        String staleNewToken = redisTemplate.opsForValue().get(STALE_PREFIX + oldToken);
        if (staleNewToken != null) {
            return staleNewToken;
        }

        // 2. 解析旧 token 获取用户信息
        LoginUser loginUser;
        String oldSessionId;
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(oldToken)
                    .getBody();
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
        var keys = redisTemplate.keys(REFRESH_PREFIX + userId + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile -pl . -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/siact/common/utils/JwtUtil.java
git commit -m "feat(auth): 重写 JwtUtil，实现滑动过期和并发安全刷新"
```

---

### Task 3: SysUserRepository 新增 findByAccount

**Files:**
- Modify: `src/main/java/com/siact/module/system/repository/SysUserRepository.java`
- Modify: `src/main/java/com/siact/module/system/repository/impl/SysUserRepositoryImpl.java`

- [ ] **Step 1: SysUserRepository 接口新增方法**

在 `SysUserRepository.java` 中添加 `findByAccount` 方法。

在现有接口中添加：

```java
SysUserEntity findByAccount(String account);
```

- [ ] **Step 2: SysUserRepositoryImpl 实现新增方法**

在 `SysUserRepositoryImpl.java` 中实现。

在现有类中添加：

```java
@Override
public SysUserEntity findByAccount(String account) {
    return mapper.selectOne(Wrappers.<SysUserEntity>lambdaQuery()
            .eq(SysUserEntity::getAccount, account)
            .last("LIMIT 1"));
}
```

需要确保 `Wrappers` 的 import 已存在（`com.baomidou.mybatisplus.core.toolkit.Wrappers`），检查现有 import 即可。

- [ ] **Step 3: 验证编译**

Run: `mvn compile -pl . -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/siact/module/system/repository/SysUserRepository.java src/main/java/com/siact/module/system/repository/impl/SysUserRepositoryImpl.java
git commit -m "feat(system): SysUserRepository 新增 findByAccount 方法"
```

---

### Task 4: 创建 AuthService 和 AuthServiceImpl

**Files:**
- Create: `src/main/java/com/siact/module/system/service/AuthService.java`
- Create: `src/main/java/com/siact/module/system/service/impl/AuthServiceImpl.java`

- [ ] **Step 1: 创建 AuthService 接口**

```java
package com.siact.module.system.service;

import com.siact.module.system.command.LoginCommand;
import com.siact.module.system.command.ModifyPasswordCommand;
import com.siact.module.system.dto.LoginUser;
import com.siact.module.system.vo.SysMenuTreeVO;

import java.util.List;

public interface AuthService {

    /**
     * 登录，返回 token 字符串
     */
    String login(LoginCommand command);

    /**
     * 登出
     */
    void logout(String token);

    /**
     * 修改密码
     */
    void modifyPassword(LoginUser currentUser, ModifyPasswordCommand command);

    /**
     * 获取当前登录用户信息
     */
    LoginUser getCurrentUser(LoginUser currentUser);

    /**
     * 获取当前用户菜单树
     */
    List<SysMenuTreeVO> getCurrentUserMenus(LoginUser currentUser);
}
```

- [ ] **Step 2: 创建 LoginCommand**

在 `module/system/command/` 下创建：

```java
package com.siact.module.system.command;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel("登录请求")
public class LoginCommand {

    @NotBlank(message = "账号不能为空")
    @ApiModelProperty(value = "账号", required = true)
    private String account;

    @NotBlank(message = "密码不能为空")
    @ApiModelProperty(value = "密码", required = true)
    private String password;
}
```

- [ ] **Step 3: 创建 ModifyPasswordCommand**

在 `module/system/command/` 下创建：

```java
package com.siact.module.system.command;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel("修改密码请求")
public class ModifyPasswordCommand {

    @NotBlank(message = "原密码不能为空")
    @ApiModelProperty(value = "原密码", required = true)
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @ApiModelProperty(value = "新密码", required = true)
    private String newPassword;
}
```

- [ ] **Step 4: 创建 AuthServiceImpl**

注意：新用户系统使用 Hutool 的 `BCrypt.hashpw()` 加密密码（不带额外 salt），所以验证时用 `BCrypt.checkpw()` 而非旧的 `passwordEncoder.matches(password + salt, ...)`。

```java
package com.siact.module.system.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.siact.common.context.LoginContext;
import com.siact.common.utils.JwtUtil;
import com.siact.module.system.command.LoginCommand;
import com.siact.module.system.command.ModifyPasswordCommand;
import com.siact.module.system.dto.LoginUser;
import com.siact.module.system.entity.SysUserEntity;
import com.siact.module.system.repository.SysUserRepository;
import com.siact.module.system.service.AuthService;
import com.siact.module.system.service.SysUserService;
import com.siact.module.system.vo.SysMenuTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserRepository userRepository;
    private final SysUserService userService;
    private final JwtUtil jwtUtil;

    @Override
    public String login(LoginCommand command) {
        SysUserEntity user = userRepository.findByAccount(command.getAccount());
        if (user == null) {
            throw new RuntimeException("账号或密码错误");
        }
        if (!Boolean.TRUE.equals(user.getStatus())) {
            throw new RuntimeException("账号已被停用");
        }
        if (!BCrypt.checkpw(command.getPassword(), user.getPassword())) {
            throw new RuntimeException("账号或密码错误");
        }

        LoginUser loginUser = new LoginUser();
        loginUser.setId(user.getId());
        loginUser.setAccount(user.getAccount());
        loginUser.setUsername(user.getUsername());

        return jwtUtil.generateToken(loginUser);
    }

    @Override
    public void logout(String token) {
        LoginUser currentUser = LoginContext.getUser();
        if (currentUser != null) {
            jwtUtil.deleteToken(token);
            jwtUtil.deleteRefreshTokens(currentUser.getId());
        }
        LoginContext.clear();
    }

    @Override
    public void modifyPassword(LoginUser currentUser, ModifyPasswordCommand command) {
        SysUserEntity user = userRepository.findByAccount(currentUser.getAccount());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!BCrypt.checkpw(command.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        SysUserEntity update = new SysUserEntity();
        update.setId(user.getId());
        update.setPassword(BCrypt.hashpw(command.getNewPassword()));
        userService.updateById(update);

        // 修改密码后强制重新登录
        jwtUtil.deleteRefreshTokens(currentUser.getId());
    }

    @Override
    public LoginUser getCurrentUser(LoginUser currentUser) {
        SysUserEntity user = userRepository.findByAccount(currentUser.getAccount());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        LoginUser loginUser = new LoginUser();
        loginUser.setId(user.getId());
        loginUser.setAccount(user.getAccount());
        loginUser.setUsername(user.getUsername());
        return loginUser;
    }

    @Override
    public List<SysMenuTreeVO> getCurrentUserMenus(LoginUser currentUser) {
        return userService.getUserMenus(currentUser.getId());
    }
}
```

- [ ] **Step 5: 验证编译**

Run: `mvn compile -pl . -q`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/siact/module/system/service/AuthService.java \
       src/main/java/com/siact/module/system/service/impl/AuthServiceImpl.java \
       src/main/java/com/siact/module/system/command/LoginCommand.java \
       src/main/java/com/siact/module/system/command/ModifyPasswordCommand.java
git commit -m "feat(auth): 创建 AuthService 登录认证服务"
```

---

### Task 5: 创建 AuthController

**Files:**
- Create: `src/main/java/com/siact/module/system/controller/AuthController.java`

- [ ] **Step 1: 创建 AuthController**

登录接口直接返回 token 字符串（用 `R<String>` 包装，data 字段就是 token 字符串）。current 和 menus 接口放在 auth 下。

```java
package com.siact.module.system.controller;

import com.siact.common.context.LoginContext;
import com.siact.common.result.R;
import com.siact.module.system.command.LoginCommand;
import com.siact.module.system.command.ModifyPasswordCommand;
import com.siact.module.system.dto.LoginUser;
import com.siact.module.system.service.AuthService;
import com.siact.module.system.vo.SysMenuTreeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Api(tags = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @ApiOperation("登录")
    @PostMapping("/login")
    public R<String> login(@Valid @RequestBody LoginCommand command) {
        String token = authService.login(command);
        return R.data(token);
    }

    @ApiOperation("登出")
    @PostMapping("/logout")
    public R<Void> logout(HttpServletRequest request) {
        String token = resolveToken(request);
        authService.logout(token);
        return R.data();
    }

    @ApiOperation("修改密码")
    @PostMapping("/modify-password")
    public R<Void> modifyPassword(@Valid @RequestBody ModifyPasswordCommand command) {
        LoginUser currentUser = LoginContext.getUser();
        authService.modifyPassword(currentUser, command);
        return R.data();
    }

    @ApiOperation("获取当前用户信息")
    @GetMapping("/current")
    public R<LoginUser> getCurrentUser() {
        LoginUser currentUser = LoginContext.getUser();
        return R.data(authService.getCurrentUser(currentUser));
    }

    @ApiOperation("获取当前用户菜单树")
    @GetMapping("/menus")
    public R<List<SysMenuTreeVO>> getCurrentUserMenus() {
        LoginUser currentUser = LoginContext.getUser();
        return R.data(authService.getCurrentUserMenus(currentUser));
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile -pl . -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/siact/module/system/controller/AuthController.java
git commit -m "feat(auth): 创建 AuthController 认证接口"
```

---

### Task 6: 重写 JwtAuthenticationFilter

**Files:**
- Rewrite: `src/main/java/com/siact/core/security/filter/JwtAuthenticationFilter.java`

- [ ] **Step 1: 重写 JwtAuthenticationFilter**

核心逻辑：从 Authorization 头提取 token → 检查 Redis 有效性 → 过期则尝试滑动刷新 → 设置 LoginContext → 通过响应头返回新 token。

```java
package com.siact.core.security.filter;

import com.siact.common.context.LoginContext;
import com.siact.common.utils.JwtUtil;
import com.siact.module.system.dto.LoginUser;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private StringRedisTemplate redisTemplate;

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/auth/login",
            "/doc.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/v2/api-docs",
            "/favicon.ico",
            "/algorithm/*",
            "/ws"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();

        // 去掉 context path 前缀进行匹配
        String contextPath = request.getContextPath();
        String path = requestUri;
        if (StringUtils.hasText(contextPath) && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }

        boolean isExcluded = EXCLUDED_PATHS.stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));

        if (isExcluded) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 1. 检查 token 是否有效
            if (jwtUtil.isTokenValid(token)) {
                LoginUser loginUser = jwtUtil.parseToken(token);
                LoginContext.setUser(loginUser);
                filterChain.doFilter(request, response);
                return;
            }

            // 2. Token 过期，尝试刷新
            String newToken = jwtUtil.refreshToken(token);

            if (newToken != null) {
                // 刷新成功：用新 token 获取用户信息
                LoginUser loginUser = jwtUtil.parseToken(newToken);
                LoginContext.setUser(loginUser);

                // 通过响应头返回新 token，前端需要更新存储的 token
                response.setHeader("X-New-Token", newToken);

                filterChain.doFilter(request, response);
            } else {
                // 刷新窗口已过期
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"success\":false,\"code\":401,\"message\":\"登录已过期，请重新登录\"}");
            }
        } catch (JwtException e) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\":false,\"code\":401,\"message\":\"Token 无效\"}");
        } finally {
            LoginContext.clear();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile -pl . -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/siact/core/security/filter/JwtAuthenticationFilter.java
git commit -m "feat(auth): 重写 JwtAuthenticationFilter，加入滑动过期和并发刷新"
```

---

### Task 7: 重写 SecurityConfig + 创建 AuthenticationEntryPointImpl

**Files:**
- Rewrite: `src/main/java/com/siact/core/security/config/SecurityConfig.java`
- Create: `src/main/java/com/siact/core/security/handler/AuthenticationEntryPointImpl.java`

- [ ] **Step 1: 创建 AuthenticationEntryPointImpl**

```java
package com.siact.core.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siact.common.result.R;
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
        response.getWriter().write(new ObjectMapper().writeValueAsString(R.unauthorized()));
    }
}
```

- [ ] **Step 2: 重写 SecurityConfig**

配置认证入口点，auth/login 路径放行，其余路径需要认证。

```java
package com.siact.core.security.config;

import com.siact.core.security.filter.JwtAuthenticationFilter;
import com.siact.core.security.handler.AuthenticationEntryPointImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .exceptionHandling()
                    .authenticationEntryPoint(authenticationEntryPoint())
                .and()
                .authorizeRequests()
                    .antMatchers("/auth/login").permitAll()
                    // Swagger/Knife4j
                    .antMatchers("/doc.html", "/webjars/**", "/swagger-resources/**", "/v2/api-docs").permitAll()
                    // WebSocket
                    .antMatchers("/ws").permitAll()
                    // 其他公开路径
                    .antMatchers("/favicon.ico").permitAll()
                    .antMatchers("/algorithm/*").permitAll()
                    .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public AuthenticationEntryPointImpl authenticationEntryPoint() {
        return new AuthenticationEntryPointImpl();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

- [ ] **Step 3: 验证编译**

Run: `mvn compile -pl . -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/siact/core/security/config/SecurityConfig.java \
       src/main/java/com/siact/core/security/handler/AuthenticationEntryPointImpl.java
git commit -m "feat(auth): 重写 SecurityConfig，新增 AuthenticationEntryPointImpl"
```

---

### Task 8: 更新外部引用

**Files:**
- Modify: `src/main/java/com/siact/core/websocket/support/StompAuthChannelInterceptor.java`
- Modify: `src/main/java/com/siact/module/process/service/impl/ProcessLogServiceImpl.java`
- Modify: `src/main/java/com/siact/common/redis/RedisConfig.java`

- [ ] **Step 1: 更新 StompAuthChannelInterceptor**

将 `LoginUntil` → `LoginContext`，`UserTokenDTO` → `LoginUser`。WebSocket 连接时从 token 解析用户并设置到 LoginContext。

```java
package com.siact.core.websocket.support;

import com.siact.common.context.LoginContext;
import com.siact.common.constant.HttpStatus;
import com.siact.common.exception.StompAuthException;
import com.siact.common.utils.JwtUtil;
import com.siact.module.system.dto.LoginUser;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@RequiredArgsConstructor
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {
    private final JwtUtil jwtUtil;

    @Nullable
    @Override
    public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (Objects.isNull(accessor)) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorization = accessor.getFirstNativeHeader("Authorization");
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new StompAuthException(HttpStatus.UNAUTHORIZED, "Authorization header missing");
            }

            String token = authorization.substring(7);
            LoginUser user = parseToken(token);

            if (Objects.isNull(user)) {
                throw new StompAuthException(401, "Invalid token");
            }
            LoginContext.setUser(user);
        }
        return message;
    }

    private LoginUser parseToken(String token) {
        if (Objects.isNull(token)) return null;
        try {
            return jwtUtil.parseToken(token);
        } catch (Exception e) {
            return null;
        }
    }
}
```

- [ ] **Step 2: 更新 ProcessLogServiceImpl**

将 `LoginUntil.getCurrentUser()` → `LoginContext.getUser()`，`UserTokenDTO` → `LoginUser`。

在文件中修改以下 import：
- 删除: `import com.siact.module.permission.dto.UserTokenDTO;`
- 删除: `import com.siact.common.utils.LoginUntil;`
- 添加: `import com.siact.common.context.LoginContext;`
- 添加: `import com.siact.module.system.dto.LoginUser;`

修改第 115 行：
```java
// 旧代码:
UserTokenDTO currentUser = LoginUntil.getCurrentUser();

// 新代码:
LoginUser currentUser = LoginContext.getUser();
```

- [ ] **Step 3: 清理 RedisConfig**

删除 `userDetailsRedisTemplate` Bean 定义和 `UserLoginInfoDTO` 的 import。

```java
package com.siact.common.redis;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
@AutoConfigureBefore(RedisAutoConfiguration.class)
public class RedisConfig extends CachingConfigurerSupport {

    @Bean
    @SuppressWarnings(value = { "unchecked", "rawtypes" })
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        FastJson2JsonRedisSerializer serializer = new FastJson2JsonRedisSerializer(Object.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisMessageListenerContainer createRedisListener(RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        return redisMessageListenerContainer;
    }
}
```

- [ ] **Step 4: 验证编译**

Run: `mvn compile -pl . -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/siact/core/websocket/support/StompAuthChannelInterceptor.java \
       src/main/java/com/siact/module/process/service/impl/ProcessLogServiceImpl.java \
       src/main/java/com/siact/common/redis/RedisConfig.java
git commit -m "refactor: 更新外部引用，LoginUntil → LoginContext"
```

---

### Task 9: 删除旧 permission 模块和 LoginUntil

**Files:**
- Delete: `src/main/java/com/siact/module/permission/` (整个目录)
- Delete: `src/main/java/com/siact/common/utils/LoginUntil.java`

- [ ] **Step 1: 搜索确认无残留引用**

搜索整个项目中是否还有对以下内容的引用：
- `com.siact.module.permission`
- `LoginUntil`
- `UserTokenDTO`（旧 permission 模块的）

如果发现残留引用，先修复再删除。

Run: `grep -r "com.siact.module.permission" src/main/java/ --include="*.java" | grep -v "^src/main/java/com/siact/module/permission/"`

Expected: 无输出（或只有 CachePreloader 中已注释的 import）

Run: `grep -r "LoginUntil" src/main/java/ --include="*.java"`

Expected: 无输出

- [ ] **Step 2: 删除旧 permission 模块**

```bash
rm -rf src/main/java/com/siact/module/permission/
```

- [ ] **Step 3: 删除 LoginUntil**

```bash
rm src/main/java/com/siact/common/utils/LoginUntil.java
```

- [ ] **Step 4: 验证编译**

Run: `mvn compile -pl . -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "refactor: 删除旧 permission 模块和 LoginUntil"
```

---

### Task 10: 添加 JWT 配置项

**Files:**
- Modify: Nacos 配置中心 `kiln-intelligent-control.yml`（需手动添加以下配置项）

- [ ] **Step 1: 在 Nacos 配置中添加新配置项**

在 Nacos 配置中心的 `kiln-intelligent-control.yml` 中添加（保留已有的 `jwt.secret` 和 `jwt.expiration`，新增以下两项）：

```yaml
jwt:
  secret: (保留现有值)
  expiration: (保留现有值)
  refresh-window: 604800000    # 7天，单位毫秒
  stale-ttl: 10000             # 10秒，旧 token 缓存窗口
```

注意：此步骤需在 Nacos 配置中心手动操作，不涉及代码变更。如果使用本地配置文件测试，可在 `bootstrap.yml` 或 `application.yml` 中临时添加。

- [ ] **Step 2: 最终验证**

Run: `mvn compile -pl . -q`
Expected: BUILD SUCCESS

确认项目能正常编译，无错误无警告。

- [ ] **Step 3: Commit（如有本地配置变更）**

```bash
git add bootstrap.yml
git commit -m "chore: 添加 jwt.refresh-window 和 jwt.stale-ttl 配置项"
```