# 认证模块重构设计

## 背景

当前登录认证模块位于 `module/permission`，基于旧 `sys_user` 表。新的 RBAC 权限体系（`module/system`，基于 `sys_user_new` 表）已开发完成，但尚未接入认证流程。本次重构将认证模块全面迁移到新用户体系，并重写认证方案。

## 决策摘要

| 决策项 | 选择 |
|--------|------|
| 重构范围 | 全面重写 |
| 安全框架 | 保留 Spring Security，重写内部实现 |
| Token 策略 | 单 token + 滑动过期 + 刷新窗口 |
| 多端登录 | 允许，同一账号多设备各自持有独立 token |
| 登录方式 | 仅账号密码 |
| 旧模块处理 | 删除整个 permission 模块 |

## API 设计

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/auth/login` | 登录，返回 token 字符串（非 JSON 包装） |
| POST | `/auth/logout` | 登出 |
| POST | `/auth/modify-password` | 修改密码 |
| GET | `/auth/current` | 获取当前登录用户信息 |
| GET | `/auth/menus` | 获取当前用户菜单树 |

前端登录后调用流程：

```
登录 → 拿到 token → 存入 localStorage
  → 路由首次跳转 → GET /auth/current → 存入 store
                  → GET /auth/menus   → 存入 store
```

### 响应结构

**登录** — 直接返回 token 字符串：

```
eyJhbGciOiJIUzI1NiJ9...
```

**当前用户** (`/auth/current`)：

```json
{
  "id": 123456789,
  "account": "admin",
  "username": "管理员",
  "orgId": 1,
  "avatar": "/avatar/default.png"
}
```

**菜单树** (`/auth/menus`)：

```json
[
  { "id": 1, "name": "系统管理", "path": "/system", "children": [] }
]
```

## Token 机制

### 生命周期

```
登录成功 → 生成 token → 存入 Redis
                         ↓
              请求携带 token → 验证有效性
                         ↓
                   ┌──── 有效 → 放行
                   │
                   └──── 过期 → 检查刷新窗口
                               ↓
                     ┌── 刷新窗口内 → 续期 token，放行
                     │
                     └── 刷新窗口过期 → 返回 401，需重新登录
```

### Redis 数据结构

```
token:{tokenValue}                   → userId (String)
TTL: token 过期时间（如 2 小时）

token:refresh:{userId}:{sessionId}   → tokenValue (String)
TTL: 刷新窗口时间（如 7 天）
```

### 并发刷新的滑动窗口

多个请求同时携带过期 token 到达时，需要防止重复刷新：

```
请求 A (token 过期) ──┐
请求 B (token 过期) ──┼── 同时到达
请求 C (token 过期) ──┘

请求 A 命中刷新窗口 → 生成新 token
                    → 旧 token 写入 stale 缓存（TTL 10s）
                    → 返回新 token

请求 B/C 发现旧 token 在 stale 缓存中 → 直接返回对应的新 token，不重复生成
```

实现要点：
- Redis `SETNX` 加锁，保证只有一个请求执行刷新
- 旧 token 缓存 key: `token:stale:{oldTokenValue}`，TTL 10s，value 为新 token
- 并发请求命中 stale 缓存时直接返回对应新 token

## 认证流程

### 登录

```
POST /auth/login { account, password }
  ├─ 1. SysUserRepository 根据 account 查询用户
  ├─ 2. 校验用户存在 + status == true
  ├─ 3. BCrypt 验证密码
  ├─ 4. JwtUtil 生成 token（claims 存 userId、account）
  ├─ 5. Redis 写入：
  │     token:{tokenValue} → userId, TTL = token 过期时间
  │     token:refresh:{userId}:{sessionId} → tokenValue, TTL = 刷新窗口
  └─ 6. 返回 token 字符串
```

### 请求认证（JwtAuthenticationFilter）

```
请求到达 → 提取 Authorization: Bearer {token}
  ├─ 无 token → 放行（由 SecurityConfig 控制路径权限）
  ├─ token 有效 → 放行，设置 SecurityContext
  └─ token 过期 → 检查刷新窗口
       ├─ 刷新窗口内：
       │   ├─ SETNX 加锁
       │   │   ├─ 成功 → 生成新 token，旧 token 写入 stale 缓存（TTL 10s）
       │   │   │        → 更新 Redis，响应头返回新 token
       │   │   └─ 失败 → 检查 stale 缓存，命中则返回对应新 token
       │   └─ 设置 SecurityContext，放行
       └─ 刷新窗口过期 → 返回 401
```

### 登出

```
POST /auth/logout
  ├─ 1. 从 SecurityContext 获取当前用户
  ├─ 2. 删除 token:{tokenValue}
  ├─ 3. 删除 token:refresh:{userId}:*
  └─ 4. 清除 SecurityContext
```

### 修改密码

```
POST /auth/modify-password { oldPassword, newPassword }
  ├─ 1. 从 SecurityContext 获取当前用户
  ├─ 2. BCrypt 验证旧密码
  ├─ 3. BCrypt 加密新密码，更新数据库
  └─ 4. 删除该用户所有 token，强制重新登录
```

## LoginContext

替代现有 `LoginUntil`，改进类型安全和性能：

```java
public class LoginContext {
    private static final ThreadLocal<UserTokenDTO> USER_HOLDER = new ThreadLocal<>();

    public static void setUser(UserTokenDTO user);
    public static UserTokenDTO getUser();
    public static Long getUserId();
    public static String getAccount();
    public static void clear();
}
```

- 直接存 `UserTokenDTO` 对象，不做 JSON 序列化/反序列化
- 提供常用字段快捷方法
- 在 `JwtAuthenticationFilter.afterCompletion()` 中确保清除，防内存泄漏

## 异常处理

新增 `AuthenticationEntryPointImpl` 处理未认证请求（返回 401）。在全局异常处理器中补充 `AuthenticationException` → 401、`AccessDeniedException` → 403 的处理，替代现有每个接口手动 try-catch 的方式。

## 文件规划

### 新增/重写

| 文件 | 操作 | 说明 |
|------|------|------|
| `module/system/controller/AuthController.java` | 新建 | 认证 API |
| `module/system/service/AuthService.java` | 新建 | 认证服务接口 |
| `module/system/service/impl/AuthServiceImpl.java` | 新建 | 认证服务实现 |
| `module/system/command/LoginCommand.java` | 新建 | 登录请求 |
| `module/system/command/ModifyPasswordCommand.java` | 新建 | 修改密码请求 |
| `module/system/vo/LoginVO.java` | 新建 | 登录响应 |
| `core/security/config/SecurityConfig.java` | 重写 | 安全配置 |
| `core/security/filter/JwtAuthenticationFilter.java` | 重写 | 加入滑动过期 + 并发刷新 |
| `core/security/handler/AuthenticationEntryPointImpl.java` | 新建 | 未认证响应 |
| `common/utils/JwtUtil.java` | 重写 | 刷新窗口 + 并发安全 |
| `common/context/LoginContext.java` | 新建 | 替代 LoginUntil |

### 删除

| 文件 | 说明 |
|------|------|
| `module/permission/` 整个目录 | 旧权限模块 |
| `common/utils/LoginUntil.java` | 被 LoginContext 替代 |