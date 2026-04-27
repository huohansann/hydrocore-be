# Python 算法 SSH 调用功能设计

## 背景

Java 应用运行在 Docker 容器中，容器内无 Python 环境。算法脚本使用 PyTorch + NVIDIA GPU，必须在物理机上运行。需要在现有 HTTP 算法调用之外，新增通过 SSH 从 Java 容器调用物理机 Python 脚本的能力。

## 方案

Java 通过 JSch（纯 Java SSH2 实现）连接物理主机，执行 Python 脚本命令，捕获 stdout 的 JSON 输出并解析为 Java 对象。

```
Java (Docker) → JSch SSH → 物理主机 shell → Python 脚本 (sys.argv) → stdout JSON → Java 解析
```

## 新增组件

| 组件 | 位置 | 职责 |
|---|---|---|
| SshUtils | `com.siact.common.utils` | SSH 连接与远程命令执行 |
| PythonAlgorithmService | `module/algorithm/services` | Python 算法调用服务接口 |
| PythonAlgorithmServiceImpl | `module/algorithm/services/impl` | 调用实现 |
| KilnProperty.Ssh | `com.siact.common.config` | SSH 连接配置 |

## 依赖

pom.xml 新增：

```xml
<dependency>
    <groupId>com.github.mwiede</groupId>
    <artifactId>jsch</artifactId>
    <version>0.2.16</version>
</dependency>
```

mwiede/jsch 是原始 com.jcraft:jsch 的活跃维护 fork，支持 JDK 8。

## 配置

### KilnProperty 扩展

在 `KilnProperty.Algorithm` 内新增 `Ssh` 内部类：

```java
@Getter @Setter
public static class Ssh {
    private String host;
    private int port = 22;
    private String username;
    private String password;           // 密码认证
    private String privateKeyPath;     // 密钥认证（与 password 二选一）
    private int timeout = 30_000;      // 连接超时 ms
    private String pythonPath = "python3";
    private String scriptBasePath;     // Python 脚本根目录
}
```

### YAML 配置示例

```yaml
spring:
  kiln:
    algorithm:
      ssh:
        host: 192.168.1.100
        port: 22
        username: algorithm
        private-key-path: /app/config/ssh/id_rsa
        timeout: 30000
        python-path: python3
        script-base-path: /home/algorithm/scripts
```

## SshUtils

静态工具类，职责：建立 SSH Session、执行远程命令、返回结果。

```java
public final class SshUtils {

    public static SshResult execute(String host, int port, String username,
                                     String password, String privateKeyPath,
                                     int timeout, String command) {
        // 1. 创建 JSch 实例
        // 2. 优先使用密钥认证（privateKeyPath 非空时），否则使用密码
        // 3. 创建 Session 并 connect
        // 4. 开启 exec channel 执行 command
        // 5. 分别读取 stdout 和 stderr（使用 Apache Commons IOUtils）
        // 6. 返回 SshResult(stdout, stderr, exitCode)
        // 7. finally 中关闭 channel 和 session
    }

    @Getter @Setter
    public static class SshResult {
        private String stdout;
        private String stderr;
        private int exitCode;
    }
}
```

错误处理：
- SSH 连接失败 / 认证失败 → 抛出 BizException
- exitCode != 0 → 日志记录 stderr，抛出 BizException

依赖：JSch（SSH 协议）、Apache Commons IO（流读取）。

## PythonAlgorithmService

### 接口

```java
public interface PythonAlgorithmService {

    /**
     * 执行远程 Python 脚本并返回解析结果
     *
     * @param scriptName Python 脚本文件名（相对于 scriptBasePath）
     * @param params     传递给脚本的参数（通过 sys.argv 传入）
     * @param returnType 期望的返回类型
     * @return 解析后的结果对象
     */
    <T> T execute(String scriptName, Map<String, String> params, Class<T> returnType);

    /**
     * 执行远程 Python 脚本并返回解析结果（支持泛型类型）
     */
    <T> T execute(String scriptName, Map<String, String> params, TypeReference<T> typeRef);
}
```

### 实现

```java
@Service
public class PythonAlgorithmServiceImpl implements PythonAlgorithmService {
    private final KilnProperty property;

    @Override
    public <T> T execute(String scriptName, Map<String, String> params, Class<T> returnType) {
        // 1. 从 property 获取 SSH 配置
        // 2. 构建命令: pythonPath + scriptBasePath + "/" + scriptName + 空格拼接 params values
        // 3. 调用 SshUtils.execute()
        // 4. 检查 exitCode
        // 5. JacksonUtils.fromJson(stdout, returnType) 返回
    }

    @Override
    public <T> T execute(String scriptName, Map<String, String> params, TypeReference<T> typeRef) {
        // 同上，最终用 JacksonUtils.fromJson(stdout, typeRef)
    }
}
```

### 参数传递约定

- Java 端 `params` 的 values 按 Map 迭代顺序作为命令行参数追加
- Python 端通过 `sys.argv[1:]` 获取参数
- Python 端**仅通过 stdout 输出一行 JSON**，不能有其他 print 输出

### 调用示例

Java 端：
```java
Map<String, String> params = new LinkedHashMap<>();
params.put("ts", "2026-04-23 10:30:00");
params.put("startTime", "2026-04-23 10:00:00");
params.put("endTime", "2026-04-23 10:30:00");

// 返回 Map
Map<String, Object> result = pythonAlgorithmService
    .execute("control_algorithm.py", params, Map.class);

// 返回指定类型
ControlResult result = pythonAlgorithmService
    .execute("control_algorithm.py", params, ControlResult.class);

// 返回泛型 List
List<TempData> result = pythonAlgorithmService
    .execute("temp_predict.py", params, new TypeReference<List<TempData>>(){});
```

Python 端：
```python
import sys, json

args = sys.argv[1:]
ts, start_time, end_time = args[0], args[1], args[2]

# ... 算法逻辑 ...

print(json.dumps({"code": "200", "result": {...}, "success": True}))
```

## 与现有架构的关系

- 现有 `AlgorithmService.callResolve()` 用于 HTTP 调用外部算法服务，保持不变
- 新增 `PythonAlgorithmService` 是独立的并行调用路径，不替代现有 HTTP 调用
- JSON 解析统一使用 `JacksonUtils`，不使用 fastjson2
- 错误处理使用现有的 `BizException`

## 文件变更清单

| 操作 | 文件 |
|---|---|
| 新增 | `com.siact.common.utils.SshUtils` |
| 新增 | `module/algorithm/services/PythonAlgorithmService` |
| 新增 | `module/algorithm/services/impl/PythonAlgorithmServiceImpl` |
| 修改 | `com.siact.common.config.KilnProperty`（新增 Ssh 内部类） |
| 修改 | `pom.xml`（新增 jsch 依赖） |