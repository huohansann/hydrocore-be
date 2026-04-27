# Python 算法 SSH 调用功能 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在现有 HTTP 算法调用之外，新增通过 JSch SSH 从 Java Docker 容器调用物理机 Python 脚本的能力。

**Architecture:** Java 端通过 JSch 纯 Java SSH 库连接物理主机，执行 Python 脚本命令（参数通过 sys.argv 传递），捕获 stdout 的 JSON 输出，使用 JacksonUtils 解析为 Java 对象。

**Tech Stack:** JSch (mwiede fork 0.2.16), Apache Commons IO, Jackson, Spring Boot ConfigurationProperties

---

## File Structure

| Operation | File | Responsibility |
|---|---|---|
| Modify | `pom.xml` | 新增 JSch 依赖 |
| Modify | `src/main/java/com/siact/common/config/KilnProperty.java` | 新增 Ssh 配置内部类 |
| Create | `src/main/java/com/siact/common/utils/SshUtils.java` | SSH 连接与远程命令执行工具类 |
| Create | `src/main/java/com/siact/module/algorithm/services/PythonAlgorithmService.java` | Python 算法调用服务接口 |
| Create | `src/main/java/com/siact/module/algorithm/services/impl/PythonAlgorithmServiceImpl.java` | 调用实现 |

---

### Task 1: 添加 JSch Maven 依赖

**Files:**
- Modify: `pom.xml`

- [ ] **Step 1: 在 pom.xml 的 `<dependencies>` 中添加 JSch 依赖**

在 `<!-- mqtt -->` 依赖之前（约第 216 行），添加：

```xml
        <!-- JSch SSH - 调用远程 Python 算法 -->
        <dependency>
            <groupId>com.github.mwiede</groupId>
            <artifactId>jsch</artifactId>
            <version>0.2.16</version>
        </dependency>
```

- [ ] **Step 2: 验证依赖解析**

Run: `cd /home/Tso/devroot/code/projects/kic-be && mvn dependency:resolve -q 2>&1 | tail -5`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add pom.xml
git commit -m "feat(algorithm): add JSch SSH dependency for Python algorithm invocation"
```

---

### Task 2: 扩展 KilnProperty 配置类

**Files:**
- Modify: `src/main/java/com/siact/common/config/KilnProperty.java`

- [ ] **Step 1: 在 KilnProperty.Algorithm 类中添加 ssh 字段和 Ssh 内部类**

在 `Algorithm` 类中（现有字段 `predictedInterval` 之后），添加 `ssh` 字段。在 `Algorithm` 类的闭合花括号之后、`IntervalControl` 类之前，添加 `Ssh` 内部类。

在 `Algorithm` 类内的 `private long predictedInterval;` 之后添加：

```java
        private Ssh ssh;
```

在 `Algorithm` 类闭合花括号之后、`IntervalControl` 类之前添加：

```java
    @Getter
    @Setter
    public static class Ssh {
        private String host;
        private int port = 22;
        private String username;
        private String password;
        private String privateKeyPath;
        private int timeout = 30_000;
        private String pythonPath = "python3";
        private String scriptBasePath;
    }
```

- [ ] **Step 2: 验证编译**

Run: `cd /home/Tso/devroot/code/projects/kic-be && mvn compile -q 2>&1 | tail -5`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/siact/common/config/KilnProperty.java
git commit -m "feat(algorithm): add SSH config to KilnProperty for Python script invocation"
```

---

### Task 3: 实现 SshUtils 工具类

**Files:**
- Create: `src/main/java/com/siact/common/utils/SshUtils.java`

- [ ] **Step 1: 创建 SshUtils.java**

```java
package com.siact.common.utils;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.siact.common.exception.BizException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public final class SshUtils {

    private SshUtils() {
    }

    public static SshResult execute(String host, int port, String username,
                                     String password, String privateKeyPath,
                                     int timeout, String command) {
        JSch jsch = new JSch();
        Session session = null;
        ChannelExec channel = null;

        try {
            // 密钥认证优先
            if (privateKeyPath != null && !privateKeyPath.isEmpty()) {
                jsch.addIdentity(privateKeyPath);
            }
            session = jsch.getSession(username, host, port);
            if (privateKeyPath == null || privateKeyPath.isEmpty()) {
                session.setPassword(password);
            }
            session.setConfig("StrictHostKeyChecking", "no");
            session.setTimeout(timeout);
            session.connect(timeout);

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.connect(timeout);

            InputStream stdout = channel.getInputStream();
            InputStream stderr = channel.getErrStream();

            String stdoutStr = IOUtils.toString(stdout, StandardCharsets.UTF_8);
            String stderrStr = IOUtils.toString(stderr, StandardCharsets.UTF_8);

            channel.connect();
            while (!channel.isClosed()) {
                Thread.sleep(100);
            }

            SshResult result = new SshResult();
            result.setStdout(stdoutStr.trim());
            result.setStderr(stderrStr.trim());
            result.setExitCode(channel.getExitStatus());

            if (result.getExitCode() != 0) {
                log.error("SSH 命令执行失败, exitCode: {}, stderr: {}, command: {}",
                        result.getExitCode(), result.getStderr(), command);
                throw new BizException("SSH 命令执行失败: " + result.getStderr());
            }

            return result;
        } catch (BizException e) {
            throw e;
        } catch (JSchException e) {
            log.error("SSH 连接失败: {}", e.getMessage(), e);
            throw new BizException("SSH 连接失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("SSH 执行异常: {}", e.getMessage(), e);
            throw new BizException("SSH 执行异常: " + e.getMessage());
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }

    @Getter
    @Setter
    public static class SshResult {
        private String stdout;
        private String stderr;
        private int exitCode;
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `cd /home/Tso/devroot/code/projects/kic-be && mvn compile -q 2>&1 | tail -5`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/siact/common/utils/SshUtils.java
git commit -m "feat(algorithm): add SshUtils for remote command execution via JSch"
```

---

### Task 4: 实现 PythonAlgorithmService 接口

**Files:**
- Create: `src/main/java/com/siact/module/algorithm/services/PythonAlgorithmService.java`

- [ ] **Step 1: 创建 PythonAlgorithmService.java**

```java
package com.siact.module.algorithm.services;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

public interface PythonAlgorithmService {

    /**
     * 执行远程 Python 脚本并返回解析结果
     *
     * @param scriptName Python 脚本文件名（相对于 scriptBasePath）
     * @param params     传递给脚本的参数（通过 sys.argv 传入）
     * @param returnType 期望的返回类型
     * @param <T>        返回类型泛型
     * @return 解析后的结果对象
     */
    <T> T execute(String scriptName, Map<String, String> params, Class<T> returnType);

    /**
     * 执行远程 Python 脚本并返回解析结果（支持泛型类型如 List&lt;T&gt;）
     *
     * @param scriptName Python 脚本文件名（相对于 scriptBasePath）
     * @param params     传递给脚本的参数（通过 sys.argv 传入）
     * @param typeRef    Jackson TypeReference，用于泛型类型
     * @param <T>        返回类型泛型
     * @return 解析后的结果对象
     */
    <T> T execute(String scriptName, Map<String, String> params, TypeReference<T> typeRef);
}
```

- [ ] **Step 2: 验证编译**

Run: `cd /home/Tso/devroot/code/projects/kic-be && mvn compile -q 2>&1 | tail -5`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/siact/module/algorithm/services/PythonAlgorithmService.java
git commit -m "feat(algorithm): add PythonAlgorithmService interface"
```

---

### Task 5: 实现 PythonAlgorithmServiceImpl

**Files:**
- Create: `src/main/java/com/siact/module/algorithm/services/impl/PythonAlgorithmServiceImpl.java`

- [ ] **Step 1: 创建 PythonAlgorithmServiceImpl.java**

```java
package com.siact.module.algorithm.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.siact.common.config.KilnProperty;
import com.siact.common.exception.BizException;
import com.siact.common.utils.JacksonUtils;
import com.siact.common.utils.SshUtils;
import com.siact.module.algorithm.services.PythonAlgorithmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class PythonAlgorithmServiceImpl implements PythonAlgorithmService {

    private final KilnProperty property;

    @Override
    public <T> T execute(String scriptName, Map<String, String> params, Class<T> returnType) {
        SshUtils.SshResult result = doExecute(scriptName, params);
        return JacksonUtils.fromJson(result.getStdout(), returnType);
    }

    @Override
    public <T> T execute(String scriptName, Map<String, String> params, TypeReference<T> typeRef) {
        SshUtils.SshResult result = doExecute(scriptName, params);
        return JacksonUtils.fromJson(result.getStdout(), typeRef);
    }

    private SshUtils.SshResult doExecute(String scriptName, Map<String, String> params) {
        KilnProperty.Algorithm.Ssh sshConfig = getSshConfig();
        String command = buildCommand(sshConfig, scriptName, params);

        log.info("调用 Python 算法: script={}, params={}", scriptName, params);
        SshUtils.SshResult result = SshUtils.execute(
                sshConfig.getHost(),
                sshConfig.getPort(),
                sshConfig.getUsername(),
                sshConfig.getPassword(),
                sshConfig.getPrivateKeyPath(),
                sshConfig.getTimeout(),
                command
        );
        log.info("Python 算法返回: script={}, stdout={}", scriptName, result.getStdout());
        return result;
    }

    private KilnProperty.Algorithm.Ssh getSshConfig() {
        KilnProperty.Algorithm algorithm = property.getAlgorithm();
        if (algorithm == null || algorithm.getSsh() == null) {
            throw new BizException("未配置 Python 算法 SSH 连接信息");
        }
        KilnProperty.Algorithm.Ssh ssh = algorithm.getSsh();
        if (StringUtils.isBlank(ssh.getHost()) || StringUtils.isBlank(ssh.getUsername())) {
            throw new BizException("Python 算法 SSH 配置不完整: 缺少 host 或 username");
        }
        if (StringUtils.isBlank(ssh.getPassword()) && StringUtils.isBlank(ssh.getPrivateKeyPath())) {
            throw new BizException("Python 算法 SSH 配置不完整: 需要配置 password 或 privateKeyPath");
        }
        return ssh;
    }

    private String buildCommand(KilnProperty.Algorithm.Ssh sshConfig, String scriptName, Map<String, String> params) {
        StringBuilder cmd = new StringBuilder();
        cmd.append(sshConfig.getPythonPath());
        cmd.append(" ").append(sshConfig.getScriptBasePath());
        if (!sshConfig.getScriptBasePath().endsWith("/")) {
            cmd.append("/");
        }
        cmd.append(scriptName);

        if (params != null && !params.isEmpty()) {
            String args = params.values().stream()
                    .map(v -> "'" + v.replace("'", "'\\''") + "'")
                    .collect(Collectors.joining(" "));
            cmd.append(" ").append(args);
        }

        return cmd.toString();
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `cd /home/Tso/devroot/code/projects/kic-be && mvn compile -q 2>&1 | tail -5`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/siact/module/algorithm/services/impl/PythonAlgorithmServiceImpl.java
git commit -m "feat(algorithm): implement PythonAlgorithmServiceImpl with SSH invocation"
```