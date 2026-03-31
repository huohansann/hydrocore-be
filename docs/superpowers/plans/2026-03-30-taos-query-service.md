# TDengine 直接查询服务实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 新建 TDengine 直接查询服务，替代数字孪生接口解决限流问题。

**Architecture:** 在 tdengine 包下创建独立服务层，使用 JDBC REST 模式直接连接 TDengine 2.4 数据库，复用现有 DTO 类型保持接口兼容。

**Tech Stack:** Spring Boot 2.6.13, TDengine JDBC 2.0.39 (REST 模式), HikariCP 连接池。

---

## 文件结构

```
src/main/java/com/siact/tdengine/
├── config/TaosConfig.java              # DataSource 配置 Bean
├── properties/TaosProperties.java      # 配置属性类
├── service/TaosDataService.java        # 服务接口定义
├── service/TaosDataServiceImpl.java    # 服务实现
├── util/TaosSqlBuilder.java            # SQL 构建工具
└── util/TaosJdbcClient.java            # JDBC 查询封装
```

**修改文件：**
- `pom.xml` - 添加 taos-jdbcdriver 依赖

---

## Task 1: 添加 TDengine JDBC 依赖

**Files:**
- Modify: `pom.xml`

- [ ] **Step 1: 在 pom.xml 添加 taos-jdbcdriver 依赖**

在 `<dependencies>` 节点中添加：

```xml
<!-- TDengine JDBC Driver - REST 模式连接 TDengine 2.x -->
<dependency>
    <groupId>com.taosdata.jdbc</groupId>
    <artifactId>taos-jdbcdriver</artifactId>
    <version>2.0.39</version>
</dependency>
```

位置：放在 MySQL 依赖之后（约第 127 行附近）。

- [ ] **Step 2: 提交依赖变更**

```bash
git add pom.xml
git commit -m "feat: 添加 TDengine JDBC REST 驱动依赖"
```

---

## Task 2: 创建配置属性类

**Files:**
- Create: `src/main/java/com/siact/tdengine/properties/TaosProperties.java`

- [ ] **Step 1: 创建 TaosProperties.java**

```java
package com.siact.tdengine.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TDengine 数据库连接配置属性
 * 从 Nacos 配置中心读取 spring.datasource.taos 配置
 */
@Data
@ConfigurationProperties(prefix = "spring.datasource.taos")
public class TaosProperties {

    /**
     * JDBC URL，REST 模式格式: jdbc:TAOS-RS://host:6041/database
     */
    private String url;

    /**
     * 用户名，默认 root
     */
    private String username = "root";

    /**
     * 密码，默认 taosdata
     */
    private String password = "taosdata";

    /**
     * 连接池最大连接数
     */
    private int maximumPoolSize = 10;

    /**
     * 连接超时时间（毫秒）
     */
    private long connectionTimeout = 5000;
}
```

- [ ] **Step 2: 提交配置属性类**

```bash
git add src/main/java/com/siact/tdengine/properties/TaosProperties.java
git commit -m "feat: 添加 TDengine 配置属性类"
```

---

## Task 3: 创建 DataSource 配置类

**Files:**
- Create: `src/main/java/com/siact/tdengine/config/TaosConfig.java`

- [ ] **Step 1: 创建 TaosConfig.java**

```java
package com.siact.tdengine.config;

import com.siact.tdengine.properties.TaosProperties;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * TDengine 数据源配置
 * 使用 HikariCP 连接池管理 JDBC REST 连接
 */
@Configuration
@EnableConfigurationProperties(TaosProperties.class)
public class TaosConfig {

    /**
     * 创建 TDengine DataSource Bean
     * 使用 REST 模式连接，无需本地安装 TDengine 客户端
     */
    @Bean(name = "taosDataSource")
    public DataSource taosDataSource(TaosProperties properties) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(properties.getUrl());
        dataSource.setUsername(properties.getUsername());
        dataSource.setPassword(properties.getPassword());
        dataSource.setMaximumPoolSize(properties.getMaximumPoolSize());
        dataSource.setConnectionTimeout(properties.getConnectionTimeout());
        dataSource.setPoolName("TaosHikariPool");
        return dataSource;
    }
}
```

- [ ] **Step 2: 提交配置类**

```bash
git add src/main/java/com/siact/tdengine/config/TaosConfig.java
git commit -m "feat: 添加 TDengine DataSource 配置类"
```

---

## Task 4: 创建 JDBC 查询封装类

**Files:**
- Create: `src/main/java/com/siact/tdengine/util/TaosJdbcClient.java`

- [ ] **Step 1: 创建 TaosJdbcClient.java**

```java
package com.siact.tdengine.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * TDengine JDBC 查询封装
 * 提供统一的查询执行和结果处理
 */
@Slf4j
@Component
public class TaosJdbcClient {

    @Resource(name = "taosDataSource")
    private DataSource dataSource;

    /**
     * 执行查询并使用映射函数处理结果
     *
     * @param sql      SQL 查询语句
     * @param mapper   结果映射函数
     * @return 查询结果列表
     */
    public <T> List<T> executeQuery(String sql, Function<ResultSet, T> mapper) {
        log.debug("执行 TDengine 查询: {}", sql);
        List<T> results = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                results.add(mapper.apply(rs));
            }

            log.debug("查询返回 {} 条记录", results.size());
            return results;

        } catch (SQLException e) {
            log.error("TDengine 查询失败: {}", e.getMessage(), e);
            return results; // 返回空列表，不抛异常
        }
    }

    /**
     * 执行查询返回单条结果
     *
     * @param sql      SQL 查询语句
     * @param mapper   结果映射函数
     * @return 单条结果，无结果时返回 null
     */
    public <T> T executeQueryOne(String sql, Function<ResultSet, T> mapper) {
        List<T> results = executeQuery(sql, mapper);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 获取数据值（处理 NULL）
     */
    public Double getDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    /**
     * 获取字符串值（处理 NULL）
     */
    public String getString(ResultSet rs, String column) throws SQLException {
        return rs.getString(column);
    }

    /**
     * 获取时间戳字符串
     */
    public String getTimestampString(ResultSet rs, String column) throws SQLException {
        java.sql.Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toString() : null;
    }
}
```

- [ ] **Step 2: 提交 JDBC 查询类**

```bash
git add src/main/java/com/siact/tdengine/util/TaosJdbcClient.java
git commit -m "feat: 添加 TDengine JDBC 查询封装类"
```

---

## Task 5: 创建 SQL 构建工具类

**Files:**
- Create: `src/main/java/com/siact/tdengine/util/TaosSqlBuilder.java`

- [ ] **Step 1: 创建 TaosSqlBuilder.java**

```java
package com.siact.tdengine.util;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TDengine SQL 语句构建工具
 * 提供安全的 SQL 构建，防止 SQL 注入
 */
public class TaosSqlBuilder {

    private static final String TABLE_NAME = "datasource";

    /**
     * 构建等时间间隔聚合查询 SQL
     *
     * @param dataCodes   数字孪生编码列表（对应 devproperty TAG）
     * @param startTime   开始时间 yyyy-MM-dd HH:mm:ss
     * @param endTime     结束时间 yyyy-MM-dd HH:mm:ss
     * @param interval    时间间隔数值
     * @param unit        时间间隔单位（s/m/h/d）
     * @param calcType    聚合类型 AVG/MAX/MIN/LAST/FIRST/SUM/COUNT
     * @return SQL 语句
     */
    public static String buildIntervalQuerySql(List<String> dataCodes, String startTime,
                                                String endTime, int interval, String unit,
                                                String calcType) {
        String aggregateFunc = getAggregateFunction(calcType);
        String intervalUnit = convertIntervalUnit(unit);

        return String.format(
            "SELECT _wstart as ts, devproperty as datacode, %s(itemvalue) as itemvalue FROM %s " +
            "WHERE devproperty IN (%s) AND ts >= '%s' AND ts <= '%s' " +
            "INTERVAL(%d%s) FILL(NULL)",
            aggregateFunc, TABLE_NAME, buildInClause(dataCodes), startTime, endTime,
            interval, intervalUnit
        );
    }

    /**
     * 构建时间段聚合查询 SQL（查询两点之间的聚合值）
     *
     * @param dataCodes   数字孪生编码列表
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param calcType    聚合类型
     * @return SQL 语句
     */
    public static String buildAggregateQuerySql(List<String> dataCodes, String startTime,
                                                  String endTime, String calcType) {
        String aggregateFunc = getAggregateFunction(calcType);

        return String.format(
            "SELECT devproperty as datacode, %s(itemvalue) as itemvalue FROM %s " +
            "WHERE devproperty IN (%s) AND ts >= '%s' AND ts <= '%s'",
            aggregateFunc, TABLE_NAME, buildInClause(dataCodes), startTime, endTime
        );
    }

    /**
     * 构建实时值查询 SQL（查询最新一条数据）
     *
     * @param dataCodes   数字孪生编码列表
     * @return SQL 语句
     */
    public static String buildLatestQuerySql(List<String> dataCodes) {
        return String.format(
            "SELECT devproperty as datacode, itemvalue FROM %s " +
            "WHERE devproperty IN (%s) ORDER BY ts DESC LIMIT 1",
            TABLE_NAME, buildInClause(dataCodes)
        );
    }

    /**
     * 构建多编码实时值查询 SQL（每个编码最新一条）
     *
     * @param dataCodes   数字孪生编码列表
     * @return SQL 语句
     */
    public static String buildLatestQuerySqlForMultiple(List<String> dataCodes) {
        // TDengine 使用 LAST_ROW 查询每个子表的最新数据
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT devproperty as datacode, LAST(itemvalue) as itemvalue FROM ")
           .append(TABLE_NAME)
           .append(" WHERE devproperty IN (")
           .append(buildInClause(dataCodes))
           .append(") GROUP BY devproperty");
        return sql.toString();
    }

    /**
     * 构建 IN 子句
     *
     * @param values   值列表
     * @return IN 子句字符串，如 'code1', 'code2'
     */
    private static String buildInClause(List<String> values) {
        return values.stream()
            .map(v -> "'" + escapeValue(v) + "'")
            .collect(Collectors.joining(", "));
    }

    /**
     * 转义值防止 SQL 注入
     *
     * @param value   原始值
     * @return 转义后的值
     */
    private static String escapeValue(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("'", "''");
    }

    /**
     * 获取聚合函数 SQL 名称
     *
     * @param calcType   计算类型
     * @return SQL 函数名
     */
    private static String getAggregateFunction(String calcType) {
        switch (calcType.toUpperCase()) {
            case "AVG":
                return "AVG";
            case "MAX":
                return "MAX";
            case "MIN":
                return "MIN";
            case "LAST":
                return "LAST";
            case "FIRST":
                return "FIRST";
            case "SUM":
                return "SUM";
            case "COUNT":
                return "COUNT";
            case "INC":
                return "SUM"; // 增量使用 SUM 计算
            case "TOTAL":
                return "SUM";
            default:
                return "AVG";
        }
    }

    /**
     * 转换时间间隔单位为 TDengine 格式
     *
     * @param unit   原始单位（Y/M/D/H/MIN/S）
     * @return TDengine INTERVAL 单位
     */
    private static String convertIntervalUnit(String unit) {
        switch (unit.toUpperCase()) {
            case "Y":
                return "y";
            case "M":
                return "m"; // 月
            case "D":
                return "d";
            case "H":
                return "h";
            case "MIN":
                return "m"; // 分钟（TDengine 用 m 表示分钟，需要根据数值区分）
            case "S":
                return "s";
            default:
                return "h";
        }
    }
}
```

- [ ] **Step 2: 提交 SQL 构建类**

```bash
git add src/main/java/com/siact/tdengine/util/TaosSqlBuilder.java
git commit -m "feat: 添加 TDengine SQL 构建工具类"
```

---

## Task 6: 创建服务接口定义

**Files:**
- Create: `src/main/java/com/siact/tdengine/service/TaosDataService.java`

- [ ] **Step 1: 创建 TaosDataService.java**

```java
package com.siact.tdengine.service;

import com.alibaba.fastjson.JSONObject;
import com.siact.api.common.api.vo.prop.NodePropFutureValQueryVo;
import com.siact.sec.dto.*;
import com.siact.sec.vo.CommonChartParamsVo;
import com.siact.sec.vo.CumulativeDataVO;

import java.util.List;

/**
 * TDengine 直接查询服务接口
 * 替代数字孪生接口，解决限流导致的数据缺失问题
 */
public interface TaosDataService {

    /**
     * 查询柱状图、折线图等图表数据(量)
     *
     * @param vo   查询参数
     * @return 图表数据结果
     */
    CommonChartResultDto queryCommonChartData(CommonChartParamsVo vo);

    /**
     * 查询某个时间段的量
     * calcType: AVG/MAX/MIN/LAST/FIRST/SUM/INC/COUNT
     *
     * @param dataCodes   数字孪生编码codes，逗号分隔
     * @param startTime   开始时间 yyyy-MM-dd HH:mm:ss
     * @param endTime     结束时间 yyyy-MM-dd HH:mm:ss
     * @param calcType    计算类型
     * @return {dataCode: value}
     */
    JSONObject queryBetweenVal(String dataCodes, String startTime, String endTime, String calcType);

    /**
     * 查询某个时间段的量（参数对象版本）
     *
     * @param params   查询参数对象
     * @return {dataCode: value}
     */
    JSONObject queryBetweenVal(IntervalValParamsDto params);

    /**
     * 查询等时间间隔的量
     *
     * @param dto   查询参数
     * @return 时间间隔数据列表
     */
    List<IntervalDataDto> queryIntervalVal(IntervalValParamsDto dto);

    /**
     * 查询实时值（最后一包数据）
     *
     * @param dataCodes   数字孪生编码codes，逗号分隔
     * @return {dataCode: value}
     */
    JSONObject queryRealValue(String dataCodes);

    /**
     * 查询节点下属性某个时间段的量
     *
     * @param dataCode        数字孪生编码code
     * @param propModelCodes  属性模型短码（对应 itemid TAG）
     * @param startTime       开始时间
     * @param endTime         结束时间
     * @param calcType        计算类型
     * @return {propModelCode: value}
     */
    JSONObject queryNoteBetweenVal(String dataCode, String propModelCodes,
                                    String startTime, String endTime, String calcType);

    /**
     * 查询节点下属性等时间间隔的量
     *
     * @param dto   查询参数
     * @return 时间间隔数据列表
     */
    List<IntervalDataDto> queryNoteIntervalVal(IntervalNoteValParamsDto dto);

    /**
     * 查询累计值
     *
     * @param vo   查询参数
     * @return 累计数据列表
     */
    List<CumulativeDataDTO> queryCumulativeData(CumulativeDataVO vo);

    /**
     * 查询预测值
     * TDengine 不支持预测，返回空列表
     *
     * @param params   查询参数
     * @return 空列表
     */
    List<IntervalDataDto> queryForecastIntervalVal(NodePropFutureValQueryVo params);
}
```

- [ ] **Step 2: 提交服务接口**

```bash
git add src/main/java/com/siact/tdengine/service/TaosDataService.java
git commit -m "feat: 添加 TDengine 查询服务接口定义"
```

---

## Task 7: 创建服务实现类（核心查询方法）

**Files:**
- Create: `src/main/java/com/siact/tdengine/service/TaosDataServiceImpl.java`

- [ ] **Step 1: 创建 TaosDataServiceImpl.java - 基础结构和 queryIntervalVal**

```java
package com.siact.tdengine.service;

import com.alibaba.fastjson.JSONObject;
import com.siact.api.common.api.vo.prop.NodePropFutureValQueryVo;
import com.siact.sec.dto.*;
import com.siact.sec.utils.CommonHandle;
import com.siact.sec.vo.CommonChartParamsVo;
import com.siact.sec.vo.CumulativeDataVO;
import com.siact.tdengine.util.TaosJdbcClient;
import com.siact.tdengine.util.TaosSqlBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TDengine 直接查询服务实现
 * 使用 JDBC REST 模式直接查询 TDengine 数据库
 */
@Slf4j
@Service
public class TaosDataServiceImpl implements TaosDataService {

    @Resource
    private TaosJdbcClient jdbcClient;

    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ========== 等时间间隔查询 ==========

    @Override
    public List<IntervalDataDto> queryIntervalVal(IntervalValParamsDto dto) {
        log.info("查询等时间间隔的量, params: {}", JSONObject.toJSONString(dto));

        List<IntervalDataDto> results = new ArrayList<>();

        try {
            String sql = TaosSqlBuilder.buildIntervalQuerySql(
                dto.getDataCodes(),
                dto.getStartTime(),
                dto.getEndTime(),
                dto.getTs(),
                dto.getTsUnit(),
                dto.getCalcType()
            );

            List<IntervalDataDto> queryResults = jdbcClient.executeQuery(sql, rs -> {
                IntervalDataDto dataDto = new IntervalDataDto();
                String dataCode = jdbcClient.getString(rs, "datacode");
                dataDto.setDataCode(dataCode);
                dataDto.setInsDataCode(dataCode);
                dataDto.setTime(formatTimestamp(jdbcClient.getString(rs, "ts")));

                Double value = jdbcClient.getDouble(rs, "itemvalue");
                dataDto.setItemVal(value != null ?
                    BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP) : null);

                return dataDto;
            });

            results.addAll(queryResults);

        } catch (Exception e) {
            log.error("查询等时间间隔的量失败: {}", e.getMessage(), e);
        }

        return results;
    }

    // ========== 时间段聚合查询 ==========

    @Override
    public JSONObject queryBetweenVal(String dataCodes, String startTime,
                                       String endTime, String calcType) {
        log.info("查询时间段聚合值, dataCodes: {}, startTime: {}, endTime: {}, calcType: {}",
                 dataCodes, startTime, endTime, calcType);

        JSONObject result = new JSONObject();

        if (StringUtils.isBlank(dataCodes) || StringUtils.isBlank(startTime) ||
            StringUtils.isBlank(endTime)) {
            log.error("参数校验不通过");
            return result;
        }

        try {
            List<String> codeList = Arrays.asList(dataCodes.split(","));
            String sql = TaosSqlBuilder.buildAggregateQuerySql(
                codeList, startTime, endTime, calcType
            );

            jdbcClient.executeQuery(sql, rs -> {
                String dataCode = jdbcClient.getString(rs, "datacode");
                Double value = jdbcClient.getDouble(rs, "itemvalue");
                result.put(dataCode, value != null ?
                    BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP) : null);
                return null; // 只收集结果，不返回对象
            });

        } catch (Exception e) {
            log.error("查询时间段聚合值失败: {}", e.getMessage(), e);
        }

        return result;
    }

    @Override
    public JSONObject queryBetweenVal(IntervalValParamsDto params) {
        return queryBetweenVal(
            params.getDataCodes().stream().collect(Collectors.joining(",")),
            params.getStartTime(),
            params.getEndTime(),
            params.getCalcType()
        );
    }

    // ========== 辅助方法 ==========

    private String formatTimestamp(String ts) {
        if (StringUtils.isBlank(ts)) {
            return null;
        }
        // TDengine 返回的时间戳格式可能需要转换
        try {
            // 如果已经是标准格式，直接返回
            if (ts.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.*")) {
                return ts.substring(0, 19); // 截取 yyyy-MM-dd HH:mm:ss
            }
            return ts;
        } catch (Exception e) {
            return ts;
        }
    }
}
```

- [ ] **Step 2: 提交基础实现**

```bash
git add src/main/java/com/siact/tdengine/service/TaosDataServiceImpl.java
git commit -m "feat: 添加 TDengine 查询服务基础实现（queryIntervalVal/queryBetweenVal）"
```

---

## Task 8: 完善服务实现类（剩余查询方法）

**Files:**
- Modify: `src/main/java/com/siact/tdengine/service/TaosDataServiceImpl.java`

- [ ] **Step 1: 添加 queryRealValue 和 queryCommonChartData 方法**

在 TaosDataServiceImpl 类中添加以下方法：

```java
// ========== 实时值查询 ==========

@Override
public JSONObject queryRealValue(String dataCodes) {
    log.info("查询实时值, dataCodes: {}", dataCodes);

    JSONObject result = new JSONObject();

    if (StringUtils.isBlank(dataCodes)) {
        log.error("查询实时值参数为空");
        return result;
    }

    try {
        List<String> codeList = Arrays.asList(dataCodes.split(","));
        String sql = TaosSqlBuilder.buildLatestQuerySqlForMultiple(codeList);

        jdbcClient.executeQuery(sql, rs -> {
            String dataCode = jdbcClient.getString(rs, "datacode");
            Double value = jdbcClient.getDouble(rs, "itemvalue");
            result.put(dataCode, value != null ?
                BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP) : null);
            return null;
        });

    } catch (Exception e) {
        log.error("查询实时值失败: {}", e.getMessage(), e);
    }

    return result;
}

// ========== 图表数据查询 ==========

@Override
public CommonChartResultDto queryCommonChartData(CommonChartParamsVo vo) {
    log.info("查询图表数据, params: {}", JSONObject.toJSONString(vo));

    // 转换参数
    IntervalValParamsDto paramsDto = new IntervalValParamsDto();
    paramsDto.setDataCodes(vo.getDataCodes());
    paramsDto.setStartTime(vo.getStartTime());
    paramsDto.setEndTime(vo.getEndTime());
    paramsDto.setTs(vo.getTs());
    paramsDto.setTsUnit(vo.getTsUnit());
    paramsDto.setCalcType(vo.getCalcType());
    paramsDto.setFormatVal(vo.getFormatVal());

    // 查询数据
    List<IntervalDataDto> dataList = queryIntervalVal(paramsDto);

    // 构建返回结果（复用现有处理逻辑）
    CommonChartParamsDto chartParamsDto = new CommonChartParamsDto();
    chartParamsDto.setDataCodes(vo.getDataCodes());
    chartParamsDto.setStartTime(vo.getStartTime());
    chartParamsDto.setEndTime(vo.getEndTime());
    chartParamsDto.setTs(vo.getTs());
    chartParamsDto.setTsUnit(vo.getTsUnit());
    chartParamsDto.setCalcType(vo.getCalcType());
    chartParamsDto.setFormatVal(vo.getFormatVal());
    chartParamsDto.setNames(vo.getNames());
    chartParamsDto.setUnits(vo.getUnits());
    chartParamsDto.setShowTables(vo.getShowTables());

    return CommonHandle.getCommonChartResultDto(chartParamsDto, dataList);
}
```

- [ ] **Step 2: 提交实时值和图表查询**

```bash
git add src/main/java/com/siact/tdengine/service/TaosDataServiceImpl.java
git commit -m "feat: 添加 queryRealValue 和 queryCommonChartData 实现"
```

---

## Task 9: 添加节点属性查询和累计值查询

**Files:**
- Modify: `src/main/java/com/siact/tdengine/service/TaosDataServiceImpl.java`
- Modify: `src/main/java/com/siact/tdengine/util/TaosSqlBuilder.java`

- [ ] **Step 1: 在 TaosSqlBuilder 添加节点属性查询 SQL 方法**

添加以下方法到 TaosSqlBuilder.java：

```java
/**
 * 构建节点属性时间段聚合查询 SQL
 * 使用 itemid TAG 过滤属性
 *
 * @param dataCode        数字孪生编码（devproperty）
 * @param propModelCodes  属性短码列表（itemid）
 * @param startTime       开始时间
 * @param endTime         结束时间
 * @param calcType        聚合类型
 * @return SQL 语句
 */
public static String buildNodeAggregateQuerySql(String dataCode, List<String> propModelCodes,
                                                 String startTime, String endTime,
                                                 String calcType) {
    String aggregateFunc = getAggregateFunction(calcType);

    return String.format(
        "SELECT itemid as propcode, %s(itemvalue) as itemvalue FROM %s " +
        "WHERE devproperty = '%s' AND itemid IN (%s) AND ts >= '%s' AND ts <= '%s'",
        aggregateFunc, TABLE_NAME, escapeValue(dataCode),
        buildInClause(propModelCodes), startTime, endTime
    );
}

/**
 * 构建节点属性等时间间隔查询 SQL
 *
 * @param dataCode        数字孪生编码
 * @param propModelCodes  属性短码列表
 * @param startTime       开始时间
 * @param endTime         结束时间
 * @param interval        时间间隔
 * @param unit            时间单位
 * @param calcType        聚合类型
 * @return SQL 语句
 */
public static String buildNodeIntervalQuerySql(String dataCode, List<String> propModelCodes,
                                                String startTime, String endTime,
                                                int interval, String unit, String calcType) {
    String aggregateFunc = getAggregateFunction(calcType);
    String intervalUnit = convertIntervalUnit(unit);

    return String.format(
        "SELECT _wstart as ts, itemid as propcode, %s(itemvalue) as itemvalue FROM %s " +
        "WHERE devproperty = '%s' AND itemid IN (%s) AND ts >= '%s' AND ts <= '%s' " +
        "INTERVAL(%d%s) FILL(NULL)",
        aggregateFunc, TABLE_NAME, escapeValue(dataCode),
        buildInClause(propModelCodes), startTime, endTime, interval, intervalUnit
    );
}
```

- [ ] **Step 2: 在 TaosDataServiceImpl 添加节点属性查询方法**

添加以下方法到 TaosDataServiceImpl.java：

```java
// ========== 节点属性查询 ==========

@Override
public JSONObject queryNoteBetweenVal(String dataCode, String propModelCodes,
                                       String startTime, String endTime, String calcType) {
    log.info("查询节点属性时间段值, dataCode: {}, propModelCodes: {}",
             dataCode, propModelCodes);

    JSONObject result = new JSONObject();

    if (StringUtils.isBlank(dataCode) || StringUtils.isBlank(propModelCodes)) {
        log.error("参数校验不通过");
        return result;
    }

    try {
        List<String> propCodes = Arrays.asList(propModelCodes.split(","));
        String sql = TaosSqlBuilder.buildNodeAggregateQuerySql(
            dataCode, propCodes, startTime, endTime, calcType
        );

        jdbcClient.executeQuery(sql, rs -> {
            String propCode = jdbcClient.getString(rs, "propcode");
            Double value = jdbcClient.getDouble(rs, "itemvalue");
            result.put(propCode, value != null ?
                BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP) : null);
            return null;
        });

    } catch (Exception e) {
        log.error("查询节点属性时间段值失败: {}", e.getMessage(), e);
    }

    return result;
}

@Override
public List<IntervalDataDto> queryNoteIntervalVal(IntervalNoteValParamsDto dto) {
    log.info("查询节点属性等时间间隔值, params: {}", JSONObject.toJSONString(dto));

    List<IntervalDataDto> results = new ArrayList<>();

    try {
        String sql = TaosSqlBuilder.buildNodeIntervalQuerySql(
            dto.getDataCode(),
            dto.getPropModelCodes(),
            dto.getStartTime(),
            dto.getEndTime(),
            dto.getTs(),
            dto.getTsUnit(),
            dto.getCalcType()
        );

        List<IntervalDataDto> queryResults = jdbcClient.executeQuery(sql, rs -> {
            IntervalDataDto dataDto = new IntervalDataDto();
            dataDto.setDataCode(jdbcClient.getString(rs, "propcode"));
            dataDto.setInsDataCode(dto.getDataCode());
            dataDto.setTime(formatTimestamp(jdbcClient.getString(rs, "ts")));

            Double value = jdbcClient.getDouble(rs, "itemvalue");
            dataDto.setItemVal(value != null ?
                BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP) : null);

            return dataDto;
        });

        results.addAll(queryResults);

    } catch (Exception e) {
        log.error("查询节点属性等时间间隔值失败: {}", e.getMessage(), e);
    }

    return results;
}

// ========== 累计值查询 ==========

@Override
public List<CumulativeDataDTO> queryCumulativeData(CumulativeDataVO vo) {
    log.info("查询累计值, params: {}", JSONObject.toJSONString(vo));

    List<CumulativeDataDTO> results = new ArrayList<>();

    // 获取累计值（使用 INC/SUM 计算）
    JSONObject dataJson = queryBetweenVal(
        vo.getDataCodes().stream().collect(Collectors.joining(",")),
        vo.getStartTime(),
        vo.getEndTime(),
        "INC"
    );

    for (String code : vo.getDataCodes()) {
        BigDecimal value = dataJson.getBigDecimal(code);
        results.add(new CumulativeDataDTO(code, value));
    }

    // 计算同比（如果需要）
    if (vo.isYoy()) {
        String yoyStartTime = calculateYoyTime(vo.getStartTime());
        String yoyEndTime = calculateYoyTime(vo.getEndTime());

        JSONObject yoyData = queryBetweenVal(
            vo.getDataCodes().stream().collect(Collectors.joining(",")),
            yoyStartTime, yoyEndTime, "INC"
        );

        calculateYoy(results, yoyData);
    }

    // 计算环比（如果需要）
    if (vo.isQoq()) {
        String qoqStartTime = calculateQoqTime(vo.getStartTime(), vo.getTimeType());
        String qoqEndTime = calculateQoqTime(vo.getEndTime(), vo.getTimeType());

        JSONObject qoqData = queryBetweenVal(
            vo.getDataCodes().stream().collect(Collectors.joining(",")),
            qoqStartTime, qoqEndTime, "INC"
        );

        calculateQoq(results, qoqData);
    }

    return results;
}

// ========== 预测值查询（不支持） ==========

@Override
public List<IntervalDataDto> queryForecastIntervalVal(NodePropFutureValQueryVo params) {
    log.warn("TDengine 不支持预测值查询，返回空列表");
    return new ArrayList<>();
}

// ========== 辅助方法 ==========

private String calculateYoyTime(String time) {
    LocalDateTime dateTime = LocalDateTime.parse(time, DATE_FORMATTER);
    return dateTime.minusYears(1).format(DATE_FORMATTER);
}

private String calculateQoqTime(String time, String timeType) {
    LocalDateTime dateTime = LocalDateTime.parse(time, DATE_FORMATTER);
    switch (timeType.toLowerCase()) {
        case "d":
            return dateTime.minusDays(1).format(DATE_FORMATTER);
        case "m":
            return dateTime.minusMonths(1).format(DATE_FORMATTER);
        case "y":
            return dateTime.minusDays(1).format(DATE_FORMATTER);
        default:
            return dateTime.minusDays(1).format(DATE_FORMATTER);
    }
}

private void calculateYoy(List<CumulativeDataDTO> results, JSONObject yoyData) {
    BigDecimal zero = BigDecimal.ZERO;
    for (CumulativeDataDTO dto : results) {
        BigDecimal currentVal = dto.getValue();
        BigDecimal yoyVal = yoyData.getBigDecimal(dto.getCode());

        if (currentVal != null && yoyVal != null && yoyVal.compareTo(zero) != 0) {
            BigDecimal ratio = currentVal.subtract(yoyVal)
                .divide(yoyVal, 4, RoundingMode.HALF_UP);
            dto.setYoy(ratio.abs());
            dto.setYoyTrend(ratio.compareTo(zero) > 0 ? "up" :
                            ratio.compareTo(zero) < 0 ? "down" : "unchg");
        }
    }
}

private void calculateQoq(List<CumulativeDataDTO> results, JSONObject qoqData) {
    BigDecimal zero = BigDecimal.ZERO;
    for (CumulativeDataDTO dto : results) {
        BigDecimal currentVal = dto.getValue();
        BigDecimal qoqVal = qoqData.getBigDecimal(dto.getCode());

        if (currentVal != null && qoqVal != null && qoqVal.compareTo(zero) != 0) {
            BigDecimal ratio = currentVal.subtract(qoqVal)
                .divide(qoqVal, 4, RoundingMode.HALF_UP);
            dto.setQoq(ratio.abs());
            dto.setQoqTrend(ratio.compareTo(zero) > 0 ? "up" :
                            ratio.compareTo(zero) < 0 ? "down" : "unchg");
        }
    }
}
```

- [ ] **Step 3: 提交完整实现**

```bash
git add src/main/java/com/siact/tdengine/service/TaosDataServiceImpl.java \
        src/main/java/com/siact/tdengine/util/TaosSqlBuilder.java
git commit -m "feat: 完善 TDengine 查询服务实现（节点属性查询、累计值查询）"
```

---

## Task 10: 添加缺失的 DTO 导入和类完善

**Files:**
- Modify: `src/main/java/com/siact/tdengine/service/TaosDataServiceImpl.java`
- Modify: `src/main/java/com/siact/tdengine/util/TaosSqlBuilder.java`

- [ ] **Step 1: 添加 CommonChartParamsDto 导入和使用**

确保 TaosDataServiceImpl.java 导入 CommonChartParamsDto：

```java
import com.siact.sec.dto.CommonChartParamsDto;
```

同时确保 CommonHandle 类能正确处理数据。

- [ ] **Step 2: 检查并修复编译依赖**

运行编译检查：

```bash
mvn compile -DskipTests
```

Expected: 编译成功，无错误。

- [ ] **Step 3: 提交修复**

```bash
git add -A
git commit -m "fix: 修复 TDengine 查询服务导入和依赖问题"
```

---

## Task 11: 创建单元测试

**Files:**
- Create: `src/test/java/com/siact/tdengine/util/TaosSqlBuilderTest.java`

- [ ] **Step 1: 创建 TaosSqlBuilderTest.java**

```java
package com.siact.tdengine.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaosSqlBuilderTest {

    @Test
    void testBuildIntervalQuerySql() {
        List<String> dataCodes = Arrays.asList("code1", "code2");
        String sql = TaosSqlBuilder.buildIntervalQuerySql(
            dataCodes, "2024-01-01 00:00:00", "2024-01-02 00:00:00",
            1, "h", "AVG"
        );

        assertNotNull(sql);
        assertTrue(sql.contains("SELECT"));
        assertTrue(sql.contains("AVG(itemvalue)"));
        assertTrue(sql.contains("INTERVAL(1h)"));
        assertTrue(sql.contains("'code1', 'code2'"));
    }

    @Test
    void testBuildAggregateQuerySql() {
        List<String> dataCodes = Arrays.asList("code1");
        String sql = TaosSqlBuilder.buildAggregateQuerySql(
            dataCodes, "2024-01-01 00:00:00", "2024-01-02 00:00:00",
            "MAX"
        );

        assertNotNull(sql);
        assertTrue(sql.contains("MAX(itemvalue)"));
        assertTrue(sql.contains("'code1'"));
    }

    @Test
    void testBuildLatestQuerySql() {
        List<String> dataCodes = Arrays.asList("code1", "code2");
        String sql = TaosSqlBuilder.buildLatestQuerySqlForMultiple(dataCodes);

        assertNotNull(sql);
        assertTrue(sql.contains("LAST(itemvalue)"));
        assertTrue(sql.contains("GROUP BY devproperty"));
    }

    @Test
    void testSqlInjectionProtection() {
        List<String> dataCodes = Arrays.asList("code'; DROP TABLE datasource;--");
        String sql = TaosSqlBuilder.buildAggregateQuerySql(
            dataCodes, "2024-01-01 00:00:00", "2024-01-02 00:00:00",
            "AVG"
        );

        // 单引号应该被转义为双单引号
        assertTrue(sql.contains("''"));
        assertFalse(sql.contains("DROP TABLE"));
    }

    @Test
    void testGetAggregateFunction() {
        // 通过反射或公开方法测试聚合函数映射
        assertEquals("AVG", TaosSqlBuilder.getAggregateFunction("AVG"));
        assertEquals("MAX", TaosSqlBuilder.getAggregateFunction("MAX"));
        assertEquals("SUM", TaosSqlBuilder.getAggregateFunction("INC"));
    }
}
```

注意：`getAggregateFunction` 方法需要改为 public 或 package-private 以便测试。

- [ ] **Step 2: 将 getAggregateFunction 改为可测试**

修改 TaosSqlBuilder.java，将 `getAggregateFunction` 方法改为 package-private：

```java
// 改为 package-private 以便测试
static String getAggregateFunction(String calcType) {
    // ... 保持原有实现
}
```

- [ ] **Step 3: 运行测试**

```bash
mvn test -Dtest=TaosSqlBuilderTest
```

Expected: 测试通过。

- [ ] **Step 4: 提交测试**

```bash
git add src/test/java/com/siact/tdengine/util/TaosSqlBuilderTest.java \
        src/main/java/com/siact/tdengine/util/TaosSqlBuilder.java
git commit -m "test: 添加 TaosSqlBuilder 单元测试"
```

---

## Task 12: 验证编译和集成

**Files:**
- All created files

- [ ] **Step 1: 运行完整编译**

```bash
mvn clean compile -DskipTests
```

Expected: 编译成功，BUILD SUCCESS。

- [ ] **Step 2: 运行所有测试**

```bash
mvn test
```

Expected: 测试通过。

- [ ] **Step 3: 最终提交**

```bash
git add -A
git commit -m "feat: 完成 TDengine 直接查询服务实现"
```

---

## Task 13: 添加 Nacos 配置说明

**Files:**
- Modify: `docs/superpowers/specs/2026-03-30-taos-query-service-design.md`（可选）

- [ ] **Step 1: 在 Nacos 添加配置**

需要在 Nacos 配置中心添加以下配置（用户手动操作）：

```yaml
spring:
  datasource:
    taos:
      url: jdbc:TAOS-RS://taosserver:6041/meos_db
      username: root
      password: taosdata
```

配置位置：`kiln-config.properties` 或新建 `taos.yml`。

- [ ] **Step 2: 更新设计文档配置说明**

记录实际配置位置和连接信息。

---

## 成功标准

1. 所有文件创建完成，编译无错误
2. SQL 构建工具测试通过
3. 服务接口与现有 DataService 对齐
4. 查询方法返回正确的 DTO 格式
5. SQL 注入防护有效

---

## 调用方替换指南

完成后，调用方需要替换：

```java
// 原调用
@Resource
private DataService dataService;

// 替换为
@Resource
private TaosDataService taosDataService;

// 方法调用保持不变
JSONObject result = taosDataService.queryBetweenVal(...);
```