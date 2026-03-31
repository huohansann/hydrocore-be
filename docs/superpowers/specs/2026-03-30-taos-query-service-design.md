---
name: taos-query-service
description: TDengine 直接查询服务设计 - 替代数字孪生接口解决限流问题
type: project
---

# TDengine 直接查询服务设计文档

## 背景

**问题描述：**
- 当前数据查询通过调用数字孪生（Digital Twin）接口进行
- 数字孪生服务从 TDengine 查询数据
- 数字孪生存在查询限流机制，导致部分数据缺失

**解决方案：**
- 新增 TDengine 直接查询服务，绕过数字孪生接口
- 直接查询 TDengine 数据库获取数据
- 完全替代现有数字孪生查询，同时保留原接口以兼容

**数据库信息：**
- TDengine 版本：2.4
- 连接协议：REST（端口 6041）
- 数据库名：meos_db
- 用户名/密码：root/taosdata（默认）

---

## 技术方案

### 依赖

添加 TDengine JDBC REST 驱动（版本 2.0.39，兼容 TDengine 2.x 服务端）：

```xml
<dependency>
    <groupId>com.taosdata.jdbc</groupId>
    <artifactId>taos-jdbcdriver</artifactId>
    <version>2.0.39</version>
</dependency>
```

**Why:** 使用 REST 模式（`jdbc:TAOS-RS://`）无需本地安装 TDengine 客户端，部署更简单。

**How to apply:** 添加到 pom.xml dependencies 节点。

### 连接方式

- JDBC URL：`jdbc:TAOS-RS://host:6041/database`
- 使用标准 JDBC 接口（Connection、PreparedStatement、ResultSet）
- 参考 td-exporter 项目的实现方式

---

## 数据表结构

**表名：** `datasource`（超级表）

| Field | Type | Length | Note |
|-------|------|--------|------|
| ts | TIMESTAMP | 8 | 时间戳（主键） |
| itemvalue | DOUBLE | 8 | 数据值 |
| currentts | TIMESTAMP | 8 | 当前时间戳 |
| modelvalue | DOUBLE | 8 | 模型值 |
| initialvalue | NCHAR | 100 | 初始值 |
| qt | INT | 4 | 质量码 |
| gateway | NCHAR | 100 | TAG - 网关 |
| devcode | NCHAR | 100 | TAG - 设备编码 |
| devproperty | NCHAR | 100 | TAG - 数字孪生编码 |
| itemid | NCHAR | 100 | TAG - 测点ID |
| tenantid | NCHAR | 100 | TAG - 租户ID |
| projectid | NCHAR | 100 | TAG - 项目ID |

**关键字段说明：**
- `itemvalue`：数据值（查询目标）
- `devproperty`：数字孪生编码（对应现有接口的 dataCode）
- `ts`：时间戳

---

## 架构设计

### 包结构

```
src/main/java/com/siact/tdengine/
├── config/
│   └── TaosConfig.java              # DataSource 配置 Bean
├── properties/
│   └── TaosProperties.java          # 配置属性类（从 Nacos 读取）
├── service/
│   ├── TaosDataService.java         # 接口定义
│   └── TaosDataServiceImpl.java     # 实现类
└── util/
│   ├── TaosSqlBuilder.java          # SQL 语句构建工具
│   └── TaosJdbcClient.java          # JDBC 连接与查询封装
```

**Why:** 职责清晰分离，配置、服务、工具各自独立，易于测试和维护。

**How to apply:** 按包结构创建对应类文件。

---

## 配置设计

### Nacos 配置新增

```yaml
spring:
  datasource:
    taos:
      url: jdbc:TAOS-RS://taosserver:6041/meos_db
      username: root
      password: taosdata
```

### TaosProperties.java

```java
@Data
@ConfigurationProperties(prefix = "spring.datasource.taos")
public class TaosProperties {
    private String url;
    private String username = "root";
    private String password = "taosdata";
}
```

### TaosConfig.java

```java
@Configuration
@EnableConfigurationProperties(TaosProperties.class)
public class TaosConfig {

    @Bean
    public DataSource taosDataSource(TaosProperties properties) {
        // 使用 HikariCP 连接池
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(properties.getUrl());
        dataSource.setUsername(properties.getUsername());
        dataSource.setPassword(properties.getPassword());
        dataSource.setMaximumPoolSize(10);
        dataSource.setConnectionTimeout(5000);
        return dataSource;
    }
}
```

---

## 接口设计

### TaosDataService.java

接口定义与现有 `DataService` 对齐，复用现有 DTO 类型：

```java
public interface TaosDataService {

    /**
     * 查询柱状图、折线图等图表数据(量)
     */
    CommonChartResultDto queryCommonChartData(CommonChartParamsVo vo);

    /**
     * 查询某个时间段的量
     * calcType: AVG/MAX/MIN/LAST/FIRST/SUM/INC/COUNT
     */
    JSONObject queryBetweenVal(String dataCodes, String startTime, String endTime, String calcType);

    /**
     * 查询某个时间段的量（参数对象版本）
     */
    JSONObject queryBetweenVal(IntervalValParamsDto parms);

    /**
     * 查询等时间间隔的量
     */
    List<IntervalDataDto> queryIntervalVal(IntervalValParamsDto dto);

    /**
     * 查询实时值（最后一包数据）
     */
    JSONObject queryRealValue(String dataCodes);

    /**
     * 查询节点下属性某个时间段的量
     */
    JSONObject queryNoteBetweenVal(String dataCode, String propModelCodes, String startTime, String endTime, String calcType);

    /**
     * 查询节点下属性等时间间隔的量
     */
    List<IntervalDataDto> queryNoteIntervalVal(IntervalNoteValParamsDto dto);

    /**
     * 查询累计值
     */
    List<CumulativeDataDTO> queryCumulativeData(CumulativeDataVO vo);

    /**
     * 查询预测值（TDengine 不支持预测，返回空列表）
     */
    List<IntervalDataDto> queryForecastIntervalVal(NodePropFutureValQueryVo params);
}
```

---

## SQL 查询设计

### 等时间间隔查询

```sql
SELECT _wstart, devproperty, AVG(itemvalue) as itemvalue
FROM datasource
WHERE devproperty IN ('code1', 'code2')
  AND ts >= '2024-01-01 00:00:00'
  AND ts <= '2024-01-02 00:00:00'
INTERVAL(1h) FILL(NULL)
```

**说明：**
- `_wstart`：窗口起始时间
- `INTERVAL(1h)`：按小时分窗口
- `FILL(NULL)`：无数据时填充 NULL

### 时间段聚合查询

```sql
SELECT devproperty, AVG(itemvalue) as itemvalue
FROM datasource
WHERE devproperty IN ('code1', 'code2')
  AND ts >= '2024-01-01 00:00:00'
  AND ts <= '2024-01-02 00:00:00'
GROUP BY devproperty
```

**支持的聚合类型：**
| calcType | SQL 函数 |
|----------|----------|
| AVG | AVG(itemvalue) |
| MAX | MAX(itemvalue) |
| MIN | MIN(itemvalue) |
| LAST | LAST(itemvalue) |
| FIRST | FIRST(itemvalue) |
| SUM | SUM(itemvalue) |
| COUNT | COUNT(itemvalue) |
| INC | SUM(itemvalue)（增量） |

### 实时值查询

```sql
SELECT LAST_ROW(ts, itemvalue, devproperty)
FROM datasource
WHERE devproperty IN ('code1', 'code2')
```

---

## 实现逻辑

### TaosDataServiceImpl 核心流程

1. **接收参数** → 转换为 TDengine 查询参数
2. **构建 SQL** → 使用 TaosSqlBuilder 生成安全 SQL
3. **执行查询** → 通过 TaosJdbcClient 执行 JDBC 查询
4. **映射结果** → ResultSet 映射为业务 DTO
5. **返回数据** → 复用现有 DTO 类型

### TaosSqlBuilder

参考 td-exporter 的 SqlBuilder：
- 参数验证防止空值
- SQL 注入防护（转义单引号）
- IN 子句构建
- 时间范围条件构建
- INTERVAL 窗口构建

### TaosJdbcClient

- 管理 DataSource 连接池
- 执行查询并处理 ResultSet
- 异常处理与日志记录
- 资源释放（Connection、Statement、ResultSet）

---

## 错误处理

- 查询失败时记录错误日志，返回空结果（不抛异常）
- 连接超时配置 5 秒
- SQL 语法错误捕获并记录

---

## 文件变更清单

### 新增文件

| 文件 | 说明 |
|------|------|
| `tdengine/config/TaosConfig.java` | DataSource 配置 Bean |
| `tdengine/properties/TaosProperties.java` | 配置属性类 |
| `tdengine/service/TaosDataService.java` | 服务接口定义 |
| `tdengine/service/TaosDataServiceImpl.java` | 服务实现 |
| `tdengine/util/TaosSqlBuilder.java` | SQL 构建工具 |
| `tdengine/util/TaosJdbcClient.java` | JDBC 查询封装 |

### 修改文件

| 文件 | 修改内容 |
|------|----------|
| `pom.xml` | 添加 taos-jdbcdriver 依赖 |
| 调用方代码 | 替换 DataService 为 TaosDataService |

---

## 成功标准

1. 所有接口方法正确实现并能查询数据
2. 查询性能满足业务需求（无数字孪生限流）
3. 返回数据格式与现有 DataService 一致
4. 错误情况正确处理，不影响系统稳定性

---

## 风险与对策

| 风险 | 对策 |
|------|------|
| TDengine 连接不稳定 | 使用连接池 + 超时配置 + 异常捕获 |
| SQL 语法差异 | 基于 td-exporter 已验证的查询方式 |
| 数据格式不一致 | 复用现有 DTO，确保格式兼容 |

---

## 参考项目

- td-exporter：已验证的 TDengine 2.x JDBC REST 查询实现