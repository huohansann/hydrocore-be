# JWT Filter 优化实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修复 JwtAuthenticationFilter 的异常处理和生命周期问题，补上 SecurityContext 设置，新增 Spring Security logout 机制。

**Architecture:** 将 JWT 解析与请求处理拆分为两个独立阶段，各自有独立的 try-finally。认证失败不自行处理响应，不设 SecurityContext，由 Spring Security 授权检查机制自动触发 AuthenticationEntryPointImpl。Logout 完全交给 Spring Security 的 LogoutHandler + LogoutSuccessHandler。

**Tech Stack:** Spring Security (WebSecurityConfigurerAdapter)、jjwt、javax.servlet、Redis

---

## 文件结构

| 文件 | 操作 | 职责 |
|------|------|------|
| `src/main/java/com/siact/core/security/filter/JwtAuthenticationFilter.java` | 修改 | JWT 认证，设置 SecurityContext + LoginContext |
| `src/main/java/com/siact/core/security/handler/LogoutSuccessHandlerImpl.java` | 新建 | 登出成功后返回 200 JSON |
| `src/main/java/com/siact/core/security/handler/LogoutHandlerImpl.java` | 新建 | 登出时清除 Redis token |
| `src/main/java/com/siact/core/security/config/SecurityConfig.java` | 修改 | 注册 Bean，配置 logout |
| `src/main/java/com/siact/module/system/controller/AuthController.java` | 修改 | 删除 logout 方法 |
| `src/test/java/com/siact/core/security/filter/JwtAuthenticationFilterTest.java` | 新建 | Filter 单元测试 |

---

### Task 1: 重构 JwtAuthenticationFilter

**Files:**
- Modify: `src/main/java/com/siact/core/security/filter/JwtAuthenticationFilter.java`

- [ ] **Step 1: 重写 JwtAuthenticationFilter**

将整个文件替换为以下内容：

```java
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
        String token = resolveToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 阶段 1: JWT 解析（与请求处理分离）
        try {
            if (jwtUtil.isTokenValid(token)) {
                setAuthentication(request, jwtUtil.parseToken(token));
            } else {
                // Token 过期，尝试刷新
                String newToken = jwtUtil.refreshToken(token);
                if (newToken != null) {
                    setAuthentication(request, jwtUtil.parseToken(newToken));
                    response.setHeader("X-New-Token", newToken);
                }
                // 刷新失败 → 不设 SecurityContext → Spring Security 拦截 → AuthenticationEntryPointImpl 处理
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

        // 阶段 2: 请求处理（独立的 try-finally 保证清理时机）
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
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -pl . -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/siact/core/security/filter/JwtAuthenticationFilter.java
git commit -m "refactor(security): rewrite JwtAuthenticationFilter with proper lifecycle and SecurityContext

- Split JWT parsing and request processing into separate try-finally blocks
- Differentiate exception types (ExpiredJwtException, SignatureException, etc.)
- Set SecurityContextHolder alongside LoginContext for Spring Security authorization
- Fix LoginContext being cleared before filterChain.doFilter on refresh path"
```

---

### Task 2: 新建 LogoutSuccessHandlerImpl

**Files:**
- Create: `src/main/java/com/siact/core/security/handler/LogoutSuccessHandlerImpl.java`

- [ ] **Step 1: 创建 LogoutSuccessHandlerImpl**

```java
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
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -pl . -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/siact/core/security/handler/LogoutSuccessHandlerImpl.java
git commit -m "feat(security): add LogoutSuccessHandlerImpl"
```

---

### Task 3: 新建 LogoutHandlerImpl

**Files:**
- Create: `src/main/java/com/siact/core/security/handler/LogoutHandlerImpl.java`

- [ ] **Step 1: 创建 LogoutHandlerImpl**

```java
package com.siact.core.security.handler;

import com.siact.module.system.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LogoutHandlerImpl implements LogoutHandler {

    @Autowired
    private AuthService authService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
                       Authentication authentication) {
        String token = resolveToken(request);
        if (token != null) {
            authService.logout(token);
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

- [ ] **Step 2: 编译验证**

Run: `mvn compile -pl . -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/siact/core/security/handler/LogoutHandlerImpl.java
git commit -m "feat(security): add LogoutHandlerImpl to clear Redis tokens on logout"
```

---

### Task 4: 更新 SecurityConfig

**Files:**
- Modify: `src/main/java/com/siact/core/security/config/SecurityConfig.java`

- [ ] **Step 1: 添加 logout 配置和新 Bean**

将整个文件替换为以下内容：

```java
package com.siact.core.security.config;

import com.siact.core.security.filter.JwtAuthenticationFilter;
import com.siact.core.security.handler.AuthenticationEntryPointImpl;
import com.siact.core.security.handler.LogoutHandlerImpl;
import com.siact.core.security.handler.LogoutSuccessHandlerImpl;
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
                .logout()
                    .logoutUrl("/auth/logout")
                    .addLogoutHandler(logoutHandler())
                    .logoutSuccessHandler(logoutSuccessHandler())
                .and()
                .authorizeRequests()
                    .antMatchers("/auth/login").permitAll()
                    .antMatchers("/doc.html", "/webjars/**", "/swagger-resources/**", "/v2/api-docs").permitAll()
                    .antMatchers("/ws").permitAll()
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
    public LogoutHandlerImpl logoutHandler() {
        return new LogoutHandlerImpl();
    }

    @Bean
    public LogoutSuccessHandlerImpl logoutSuccessHandler() {
        return new LogoutSuccessHandlerImpl();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -pl . -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/siact/core/security/config/SecurityConfig.java
git commit -m "feat(security): configure Spring Security logout with custom handlers"
```

---

### Task 5: 清理 AuthController

**Files:**
- Modify: `src/main/java/com/siact/module/system/controller/AuthController.java`

- [ ] **Step 1: 删除 logout 方法和 resolveToken 辅助方法**

删除第 32-37 行的 `logout` 方法和第 61-67 行的 `resolveToken` 方法（`resolveToken` 只被 `logout` 使用，其他方法如 `modifyPassword` 直接从 `LoginContext` 获取用户信息）。

最终文件内容：

```java
package com.siact.module.system.controller;

import com.siact.common.context.LoginContext;
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
    public String login(@Valid @RequestBody LoginCommand command) {
        return authService.login(command);
    }

    @ApiOperation("修改密码")
    @PostMapping("/modify-password")
    public void modifyPassword(@Valid @RequestBody ModifyPasswordCommand command, HttpServletRequest request) {
        String token = resolveToken(request);
        LoginUser currentUser = LoginContext.getUser();
        authService.modifyPassword(token, currentUser, command);
    }

    @ApiOperation("获取当前用户信息")
    @GetMapping("/current")
    public LoginUser getCurrentUser() {
        LoginUser currentUser = LoginContext.getUser();
        return authService.getCurrentUser(currentUser);
    }

    @ApiOperation("获取当前用户菜单树")
    @GetMapping("/menus")
    public List<SysMenuTreeVO> getCurrentUserMenus() {
        LoginUser currentUser = LoginContext.getUser();
        return authService.getCurrentUserMenus(currentUser);
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

注意：`resolveToken` 仍被 `modifyPassword` 使用，所以保留。

- [ ] **Step 2: 编译验证**

Run: `mvn compile -pl . -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/siact/module/system/controller/AuthController.java
git commit -m "refactor(auth): remove logout from AuthController, handled by Spring Security"
```

---

### Task 6: 编译验证 + 最终检查

- [ ] **Step 1: 全量编译**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 2: 检查无遗留引用**

Run: `grep -rn "authService.logout" src/main/java/ --include="*.java" | grep -v LogoutHandlerImpl`
Expected: 无结果（logout 只在 LogoutHandlerImpl 中调用）
