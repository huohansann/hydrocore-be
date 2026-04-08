# JWT Filter 优化设计

## 背景

当前 `JwtAuthenticationFilter` 存在三个问题：
1. try-catch 未有效处理 — `catch(JwtException)` 吞掉所有异常，调用方无感知
2. 异常类型未区分 — `ExpiredJwtException`、`SignatureException` 等不同异常应区分处理
3. finally 导致上下文提前清除 — token 刷新成功时，`LoginContext.clear()` 在 `filterChain.doFilter` 之前执行

同时需要：
- 补上 Spring Security 的 `SecurityContext` 设置，支持 `@PreAuthorize` 等注解授权
- 添加 Spring Security 的 logout 机制（`LogoutHandler` + `LogoutSuccessHandler`）
- 在 SecurityConfig 中配置 logout 端点

## 设计决策

- **认证失败不自行处理响应**，不设 SecurityContext 即可，由 Spring Security 的授权检查机制自动触发 `AuthenticationEntryPointImpl`
- **异常分类型 catch**，只加日志不抛出，不吞掉
- **filterChain.doFilter 与 JWT 解析分离**，各自独立 try-finally

## 变更清单

### 1. JwtAuthenticationFilter.java（修改）

将 JWT 解析和请求处理拆分为两个独立阶段：

```
阶段 1: JWT 解析（try-catch）
  ├─ token 有效 → setAuthentication
  ├─ token 过期 → 尝试刷新
  │   ├─ 刷新成功 → setAuthentication + 设置 X-New-Token header
  │   └─ 刷新失败 → 不设 SecurityContext（Spring Security 拦截）
  └─ 异常 → 分类型 catch，加日志，不设 SecurityContext

阶段 2: 请求处理（独立的 try-finally）
  ├─ filterChain.doFilter(request, response)
  └─ finally → clearContext（同时清除 LoginContext 和 SecurityContext）
```

异常处理策略：
- `ExpiredJwtException` — debug 级别日志（正常过期场景）
- `SignatureException` — warn 级别日志（可能是篡改攻击）
- `MalformedJwtException` — warn 级别日志（格式错误）
- `JwtException` — warn 级别日志（兜底）

`setAuthentication` 方法同时设置：
- `LoginContext.setUser(loginUser)` — 业务代码使用
- `SecurityContextHolder.getContext().setAuthentication(...)` — Spring Security 授权使用

`clearContext` 方法同时清除两者。

### 2. LogoutHandlerImpl.java（新建）

实现 Spring Security 的 `LogoutHandler` 接口，负责：
- 从 `Authorization` header 提取 Bearer token
- 调用 `authService.logout(token)` 清除 Redis 中的 token 和刷新窗口

### 3. LogoutSuccessHandlerImpl.java（新建）

实现 Spring Security 的 `LogoutSuccessHandler` 接口，负责：
- 返回 HTTP 200 + JSON 成功响应

### 4. SecurityConfig.java（修改）

- 添加 `.logout()` 配置：
  - `logoutUrl("/auth/logout")`
  - `addLogoutHandler(logoutHandler())`
  - `logoutSuccessHandler(logoutSuccessHandler())`
- 注册三个新 Bean：`JwtAuthenticationFilter`（已有）、`LogoutHandlerImpl`、`LogoutSuccessHandlerImpl`

### 5. AuthController.java（修改）

- 删除 `logout()` 方法，登出完全由 Spring Security 处理

## 文件影响

| 文件 | 操作 |
|------|------|
| `core/security/filter/JwtAuthenticationFilter.java` | 修改 |
| `core/security/handler/LogoutHandlerImpl.java` | 新建 |
| `core/security/handler/LogoutSuccessHandlerImpl.java` | 新建 |
| `core/security/config/SecurityConfig.java` | 修改 |
| `module/system/controller/AuthController.java` | 修改 |

## 异常流转路径

```
JwtAuthenticationFilter
  → catch 异常，不设 SecurityContext
  → filterChain.doFilter 继续
  → FilterSecurityInterceptor 检查授权
  → 发现 anyRequest().authenticated() 但无认证
  → 抛出 InsufficientAuthenticationException
  → ExceptionTranslationFilter 捕获
  → 调用 AuthenticationEntryPointImpl.commence()
  → 返回 401 响应
```
