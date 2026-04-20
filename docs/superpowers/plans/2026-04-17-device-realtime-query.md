# 设备实时数据查询与导出 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 device 模块新增 TDengine 时序数据查询和导出功能，支持按点位/设备/属性/时间筛选，支持原始和聚合两种查询模式，导出时根据数据量自动分级处理。

**Architecture:** 新增 `DeviceRealtimeController` → `DeviceRealtimeService` → `DeviceMappingRepository`（查映射）+ `TaosJdbcClient`（查 TDengine）。查询流程：先根据前端条件从 device_mapping 获取 propCode 列表，再用 propCode 查 TDengine，最后回填设备信息。导出采用三级策略（全量/分批流式/分批临时文件）。

**Tech Stack:** Spring Boot, MyBatis-Plus, TDengine JDBC, EasyPoi, Lombok, Swagger

---

### Task 1: 基础 VO 和 Query 类

**Files:**
- Create: `src/main/java/com/siact/module/device/vo/SelectOptionVO.java`
- Create: `src/main/java/com/siact/module/device/vo/DeviceRealtimeVO.java`
- Create: `src/main/java/com/siact/module/device/query/DeviceRealtimeQuery.java`

- [ ] **Step 1: 创建 SelectOptionVO**

```java
// src/main/java/com/siact/module/device/vo/SelectOptionVO.java
package com.siact.module.device.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "下拉选项")
public class SelectOptionVO {

    @ApiModelProperty("显示文本")
    private String label;

    @ApiModelProperty("实际值")
    private String value;
}
```

- [ ] **Step 2: 创建 DeviceRealtimeVO**

```java
// src/main/java/com/siact/module/device/vo/DeviceRealtimeVO.java
package com.siact.module.device.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(description = "设备实时数据视图对象")
public class DeviceRealtimeVO {

    @ApiModelProperty("点位ID")
    private String itemId;

    @ApiModelProperty("属性名称")
    private String propName;

    @ApiModelProperty("设备编码")
    private String deviceCode;

    @ApiModelProperty("设备名称")
    private String deviceName;

    @ApiModelProperty("时间戳")
    private String ts;

    @ApiModelProperty("数值")
    private BigDecimal itemValue;
}
```

- [ ] **Step 3: 创建 DeviceRealtimeQuery**

```java
// src/main/java/com/siact/module/device/query/DeviceRealtimeQuery.java
package com.siact.module.device.query;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "设备实时数据查询条件")
public class DeviceRealtimeQuery {

    @ApiModelProperty("点位ID列表")
    private List<String> itemIds;

    @ApiModelProperty("属性名称(模糊搜索)")
    private String propName;

    @ApiModelProperty("设备编码列表")
    private List<String> deviceCodes;

    @NotBlank(message = "查询开始时间不能为空")
    @ApiModelProperty(value = "查询开始时间", required = true, example = "2025-01-01 00:00:00")
    private String startTime;

    @NotBlank(message = "查询结束时间不能为空")
    @ApiModelProperty(value = "查询结束时间", required = true, example = "2025-01-01 23:59:59")
    private String endTime;

    @NotBlank(message = "查询模式不能为空")
    @ApiModelProperty(value = "查询模式: raw/m/h/d", required = true)
    private String tsUnit;

    @ApiModelProperty("聚合类型: AVG/MAX/MIN/LAST（聚合模式必填）")
    private String calcType;
}
```

注意：`DeviceRealtimeQuery` 不继承 `PageQuery`，因为分页参数和导出格式参数由 Controller 层单独接收（查询用 POST body，导出用 GET params），避免混用。

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/siact/module/device/vo/SelectOptionVO.java \
        src/main/java/com/siact/module/device/vo/DeviceRealtimeVO.java \
        src/main/java/com/siact/module/device/query/DeviceRealtimeQuery.java
git commit -m "feat(device): add DeviceRealtimeQuery, DeviceRealtimeVO, SelectOptionVO"
```

---

### Task 2: 扩展 DeviceMappingRepository 查询方法

**Files:**
- Modify: `src/main/java/com/siact/module/device/repository/DeviceMappingRepository.java`
- Modify: `src/main/java/com/siact/module/device/repository/impl/DeviceMappingRepositoryImpl.java`

- [ ] **Step 1: 在 Repository 接口新增方法**

在 `DeviceMappingRepository.java` 接口末尾（`findByPropCode` 方法之后）添加：

```java
List<String> findPropCodesByConditions(List<String> itemIds, String propName, List<String> deviceCodes);

List<DeviceMappingEntity> findByPropCodes(List<String> propCodes);

List<String> findAllItemIds();

List<String> findAllDeviceCodes();

List<DeviceMappingEntity> findDistinctDeviceNames();
```

- [ ] **Step 2: 在 RepositoryImpl 实现新增方法**

在 `DeviceMappingRepositoryImpl.java` 的 `buildQueryWrapper` 方法之后添加：

```java
@Override
public List<String> findPropCodesByConditions(List<String> itemIds, String propName, List<String> deviceCodes) {
    LambdaQueryWrapper<DeviceMappingEntity> wrapper = Wrappers.<DeviceMappingEntity>lambdaQuery()
            .in(itemIds != null && !itemIds.isEmpty(), DeviceMappingEntity::getItemId, itemIds)
            .like(org.apache.commons.lang3.StringUtils.isNotBlank(propName), DeviceMappingEntity::getPropName, propName)
            .in(deviceCodes != null && !deviceCodes.isEmpty(), DeviceMappingEntity::getDeviceCode, deviceCodes)
            .select(DeviceMappingEntity::getPropCode);
    return mapper.selectList(wrapper).stream()
            .map(DeviceMappingEntity::getPropCode)
            .filter(org.apache.commons.lang3.StringUtils::isNotBlank)
            .distinct()
            .collect(java.util.stream.Collectors.toList());
}

@Override
public List<DeviceMappingEntity> findByPropCodes(List<String> propCodes) {
    if (propCodes == null || propCodes.isEmpty()) {
        return java.util.Collections.emptyList();
    }
    return mapper.selectList(Wrappers.<DeviceMappingEntity>lambdaQuery()
            .in(DeviceMappingEntity::getPropCode, propCodes));
}

@Override
public List<String> findAllItemIds() {
    return mapper.selectList(Wrappers.<DeviceMappingEntity>lambdaQuery()
            .select(DeviceMappingEntity::getItemId)
            .isNotNull(DeviceMappingEntity::getItemId)
            .orderByAsc(DeviceMappingEntity::getItemId))
            .stream()
            .map(DeviceMappingEntity::getItemId)
            .distinct()
            .collect(java.util.stream.Collectors.toList());
}

@Override
public List<String> findAllDeviceCodes() {
    return mapper.selectList(Wrappers.<DeviceMappingEntity>lambdaQuery()
            .select(DeviceMappingEntity::getDeviceCode)
            .isNotNull(DeviceMappingEntity::getDeviceCode)
            .orderByAsc(DeviceMappingEntity::getDeviceCode))
            .stream()
            .map(DeviceMappingEntity::getDeviceCode)
            .distinct()
            .collect(java.util.stream.Collectors.toList());
}

@Override
public List<DeviceMappingEntity> findDistinctDeviceNames() {
    return mapper.selectList(Wrappers.<DeviceMappingEntity>lambdaQuery()
            .select(DeviceMappingEntity::getDeviceName, DeviceMappingEntity::getDeviceCode)
            .isNotNull(DeviceMappingEntity::getDeviceName)
            .groupBy(DeviceMappingEntity::getDeviceName, DeviceMappingEntity::getDeviceCode)
            .orderByAsc(DeviceMappingEntity::getDeviceName));
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/siact/module/device/repository/DeviceMappingRepository.java \
        src/main/java/com/siact/module/device/repository/impl/DeviceMappingRepositoryImpl.java
git commit -m "feat(device): add query methods to DeviceMappingRepository for realtime data"
```

---

### Task 3: Service 接口和实现 — 下拉选项 + 查询逻辑

**Files:**
- Create: `src/main/java/com/siact/module/device/service/DeviceRealtimeService.java`
- Create: `src/main/java/com/siact/module/device/service/impl/DeviceRealtimeServiceImpl.java`

- [ ] **Step 1: 创建 Service 接口**

```java
// src/main/java/com/siact/module/device/service/DeviceRealtimeService.java
package com.siact.module.device.service;

import com.siact.common.vo.PageVO;
import com.siact.module.device.query.DeviceRealtimeQuery;
import com.siact.module.device.vo.DeviceRealtimeVO;
import com.siact.module.device.vo.SelectOptionVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface DeviceRealtimeService {

    List<SelectOptionVO> listItemIds();

    List<SelectOptionVO> listDeviceNames();

    PageVO<DeviceRealtimeVO> query(DeviceRealtimeQuery query, int page, int pageSize);

    void export(DeviceRealtimeQuery query, String format, HttpServletResponse response);
}
```

- [ ] **Step 2: 创建 Service 实现 — 下拉选项方法**

```java
// src/main/java/com/siact/module/device/service/impl/DeviceRealtimeServiceImpl.java
package com.siact.module.device.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.siact.common.exception.BizException;
import com.siact.common.vo.PageVO;
import com.siact.module.device.entity.DeviceMappingEntity;
import com.siact.module.device.mapper.DeviceMappingMapper;
import com.siact.module.device.query.DeviceRealtimeQuery;
import com.siact.module.device.repository.DeviceMappingRepository;
import com.siact.module.device.service.DeviceRealtimeService;
import com.siact.module.device.vo.DeviceRealtimeVO;
import com.siact.module.device.vo.SelectOptionVO;
import com.siact.tdengine.util.TaosJdbcClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class DeviceRealtimeServiceImpl implements DeviceRealtimeService {

    private final DeviceMappingRepository deviceMappingRepository;
    private final TaosJdbcClient jdbcClient;

    @Override
    public List<SelectOptionVO> listItemIds() {
        return deviceMappingRepository.findAllItemIds().stream()
                .map(id -> new SelectOptionVO(id, id))
                .collect(Collectors.toList());
    }

    @Override
    public List<SelectOptionVO> listDeviceNames() {
        return deviceMappingRepository.findDistinctDeviceNames().stream()
                .map(e -> new SelectOptionVO(e.getDeviceName(), e.getDeviceCode()))
                .collect(Collectors.toList());
    }
```

- [ ] **Step 3: 创建 Service 实现 — 查询方法**

在 `DeviceRealtimeServiceImpl.java` 中 `listDeviceNames()` 之后添加：

```java
    @Override
    public PageVO<DeviceRealtimeVO> query(DeviceRealtimeQuery query, int page, int pageSize) {
        // 1. 根据 query 条件查 device_mapping，获取 propCode 列表
        List<String> propCodes = deviceMappingRepository.findPropCodesByConditions(
                query.getItemIds(), query.getPropName(), query.getDeviceCodes());
        if (propCodes.isEmpty()) {
            return PageVO.empty();
        }

        // 2. 构建查询 SQL，先 COUNT 总条数
        String countSql = buildCountSql(propCodes, query.getStartTime(), query.getEndTime(), query.getTsUnit(), query.getCalcType());
        Long total = queryCount(countSql);
        if (total == 0) {
            return PageVO.empty();
        }

        // 3. 分页查询
        long offset = (long) (page - 1) * pageSize;
        String dataSql = buildDataSql(propCodes, query.getStartTime(), query.getEndTime(),
                query.getTsUnit(), query.getCalcType(), offset, pageSize);
        List<DeviceRealtimeVO> records = queryData(dataSql, propCodes);

        long pages = (total + pageSize - 1) / pageSize;
        return PageVO.<DeviceRealtimeVO>builder()
                .current((long) page)
                .size((long) pageSize)
                .pages(pages)
                .total(total)
                .records(records)
                .build();
    }
```

- [ ] **Step 4: 创建 Service 实现 — 导出方法（分级策略）**

在 `DeviceRealtimeServiceImpl.java` 中 `query()` 之后添加：

```java
    // 导出分级阈值
    private static final int SMALL_THRESHOLD = 50_000;
    private static final int LARGE_THRESHOLD = 500_000;
    private static final int BATCH_SIZE = 10_000;

    @Override
    public void export(DeviceRealtimeQuery query, String format, HttpServletResponse response) {
        List<String> propCodes = deviceMappingRepository.findPropCodesByConditions(
                query.getItemIds(), query.getPropName(), query.getDeviceCodes());
        if (propCodes.isEmpty()) {
            throw new BizException("没有匹配的点位数据");
        }

        // 先 COUNT 获取总条数
        String countSql = buildCountSql(propCodes, query.getStartTime(), query.getEndTime(), query.getTsUnit(), query.getCalcType());
        Long total = queryCount(countSql);
        if (total == 0) {
            throw new BizException("没有查询到数据");
        }

        log.info("导出数据量: {}, 使用格式: {}", total, format);

        if (total <= SMALL_THRESHOLD) {
            exportSmall(propCodes, query, format, response);
        } else if (total <= LARGE_THRESHOLD) {
            exportMedium(propCodes, query, format, response, total);
        } else {
            exportLarge(propCodes, query, format, response, total);
        }
    }

    /**
     * 小数据量：全量内存写入
     */
    private void exportSmall(List<String> propCodes, DeviceRealtimeQuery query,
                             String format, HttpServletResponse response) {
        String dataSql = buildDataSql(propCodes, query.getStartTime(), query.getEndTime(),
                query.getTsUnit(), query.getCalcType(), 0, Integer.MAX_VALUE);
        List<DeviceRealtimeVO> allData = queryData(dataSql, propCodes);
        writeToResponse(allData, format, response);
    }

    /**
     * 中等数据量：分批查询 + 流式写入
     */
    private void exportMedium(List<String> propCodes, DeviceRealtimeQuery query,
                              String format, HttpServletResponse response, long total) {
        // 初始化 response header
        setResponseHeaders(format, response);

        try {
            List<DeviceRealtimeVO> batch;
            for (long offset = 0; offset < total; offset += BATCH_SIZE) {
                int limit = (int) Math.min(BATCH_SIZE, total - offset);
                String dataSql = buildDataSql(propCodes, query.getStartTime(), query.getEndTime(),
                        query.getTsUnit(), query.getCalcType(), offset, limit);
                batch = queryData(dataSql, propCodes);
                if (batch.isEmpty()) break;

                writeBatchToStream(batch, format, response.getOutputStream(), offset == 0);
            }
            response.getOutputStream().flush();
        } catch (Exception e) {
            throw new BizException("导出失败: " + e.getMessage());
        }
    }

    /**
     * 大数据量：分批查询 + 临时文件 + 流式输出
     */
    private void exportLarge(List<String> propCodes, DeviceRealtimeQuery query,
                             String format, HttpServletResponse response, long total) {
        java.io.File tempFile = null;
        try {
            String suffix = getTempFileSuffix(format);
            tempFile = java.io.File.createTempFile("realtime_export_", suffix);
            log.info("大数据量导出，使用临时文件: {}", tempFile.getAbsolutePath());

            // 分批写入临时文件
            try (java.io.BufferedWriter writer = new java.io.BufferedWriter(
                    new java.io.OutputStreamWriter(new java.io.FileOutputStream(tempFile), "UTF-8"))) {
                List<DeviceRealtimeVO> batch;
                for (long offset = 0; offset < total; offset += BATCH_SIZE) {
                    int limit = (int) Math.min(BATCH_SIZE, total - offset);
                    String dataSql = buildDataSql(propCodes, query.getStartTime(), query.getEndTime(),
                            query.getTsUnit(), query.getCalcType(), offset, limit);
                    batch = queryData(dataSql, propCodes);
                    if (batch.isEmpty()) break;
                    writeBatchToWriter(batch, format, writer, offset == 0);
                }
            }

            // 从临时文件流式输出到 response
            setResponseHeaders(format, response);
            try (java.io.InputStream in = new java.io.BufferedInputStream(new java.io.FileInputStream(tempFile));
                 java.io.OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            }
        } catch (Exception e) {
            throw new BizException("导出失败: " + e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                log.info("临时文件清理: {}, 删除{}", tempFile.getAbsolutePath(), deleted ? "成功" : "失败");
            }
        }
    }
```

- [ ] **Step 5: 创建 Service 实现 — SQL 构建和结果映射方法**

在 `DeviceRealtimeServiceImpl.java` 中添加私有方法：

```java
    // ========== SQL 构建 ==========

    private String buildCountSql(List<String> propCodes, String startTime, String endTime, String tsUnit, String calcType) {
        String fromWhere = buildFromWhere(propCodes, startTime, endTime);
        if ("raw".equalsIgnoreCase(tsUnit)) {
            return "SELECT COUNT(*) FROM " + fromWhere;
        }
        String aggFunc = getAggregateFunction(calcType);
        return "SELECT COUNT(*) FROM (SELECT _wstartts as ts FROM " + fromWhere
                + " INTERVAL(1" + convertIntervalUnit(tsUnit) + ") FILL(NULL))";
    }

    private String buildDataSql(List<String> propCodes, String startTime, String endTime,
                                String tsUnit, String calcType, long offset, int limit) {
        String fromWhere = buildFromWhere(propCodes, startTime, endTime);

        if ("raw".equalsIgnoreCase(tsUnit)) {
            return "SELECT ts, devproperty as datacode, itemvalue FROM " + fromWhere
                    + " ORDER BY ts DESC LIMIT " + limit + " OFFSET " + offset;
        }

        String aggFunc = getAggregateFunction(calcType);
        if ("LAST".equals(aggFunc)) {
            // LAST 不支持 INTERVAL
            return "SELECT ts, devproperty as datacode, itemvalue FROM " + fromWhere
                    + " ORDER BY ts DESC LIMIT " + limit + " OFFSET " + offset;
        }

        return "SELECT _wstartts as ts, devproperty as datacode, " + aggFunc + "(itemvalue) as itemvalue FROM "
                + fromWhere + " INTERVAL(1" + convertIntervalUnit(tsUnit) + ") FILL(NULL)"
                + " LIMIT " + limit + " OFFSET " + offset;
    }

    private String buildFromWhere(List<String> propCodes, String startTime, String endTime) {
        String inClause = propCodes.stream()
                .map(v -> "'" + v.replace("'", "''") + "'")
                .collect(Collectors.joining(", "));
        return "datasource WHERE devproperty IN (" + inClause
                + ") AND ts >= '" + startTime + "' AND ts <= '" + endTime + "' ";
    }

    private String getAggregateFunction(String calcType) {
        if (calcType == null) return "AVG";
        switch (calcType.toUpperCase()) {
            case "AVG": return "AVG";
            case "MAX": return "MAX";
            case "MIN": return "MIN";
            case "LAST": return "LAST";
            case "FIRST": return "FIRST";
            case "SUM": return "SUM";
            case "COUNT": return "COUNT";
            default: return "AVG";
        }
    }

    private String convertIntervalUnit(String tsUnit) {
        if (tsUnit == null) return "h";
        switch (tsUnit.toLowerCase()) {
            case "m": return "m";
            case "h": return "h";
            case "d": return "d";
            default: return "h";
        }
    }

    // ========== 查询执行 ==========

    private Long queryCount(String sql) {
        try {
            return jdbcClient.executeQueryOne(sql, rs -> rs.getLong(1));
        } catch (Exception e) {
            log.error("COUNT 查询失败: {}", e.getMessage(), e);
            return 0L;
        }
    }

    private List<DeviceRealtimeVO> queryData(String sql, List<String> propCodes) {
        // 批量获取 propCode → DeviceMapping 的映射
        Map<String, DeviceMappingEntity> mappingMap = deviceMappingRepository.findByPropCodes(propCodes).stream()
                .collect(Collectors.toMap(DeviceMappingEntity::getPropCode, e -> e, (a, b) -> a));

        return jdbcClient.executeQuery(sql, rs -> {
            DeviceRealtimeVO vo = new DeviceRealtimeVO();
            String dataCode = jdbcClient.getString(rs, "datacode");
            vo.setTs(formatTimestamp(jdbcClient.getString(rs, "ts")));
            Double value = jdbcClient.getDouble(rs, "itemvalue");
            vo.setItemValue(value != null ? BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP) : null);

            // 回填设备信息
            DeviceMappingEntity mapping = mappingMap.get(dataCode);
            if (mapping != null) {
                vo.setItemId(mapping.getItemId());
                vo.setPropName(mapping.getPropName());
                vo.setDeviceCode(mapping.getDeviceCode());
                vo.setDeviceName(mapping.getDeviceName());
            }
            return vo;
        });
    }

    private String formatTimestamp(String ts) {
        if (StringUtils.isBlank(ts)) return null;
        try {
            if (ts.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.*")) {
                return ts.substring(0, 19);
            }
            return ts;
        } catch (Exception e) {
            return ts;
        }
    }

    // ========== 导出写入 ==========

    private void writeToResponse(List<DeviceRealtimeVO> data, String format, HttpServletResponse response) {
        switch (format.toLowerCase()) {
            case "excel":
                writeExcel(data, response);
                break;
            case "csv":
                writeCsv(data, response);
                break;
            case "json":
                writeJson(data, response);
                break;
            default:
                throw new BizException("不支持的导出格式: " + format + "，仅支持 excel/csv/json");
        }
    }

    private void writeExcel(List<DeviceRealtimeVO> data, HttpServletResponse response) {
        List<cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity> headList = new ArrayList<>();
        headList.add(new cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity("点位ID", "itemId", 20));
        headList.add(new cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity("属性名称", "propName", 20));
        headList.add(new cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity("设备编码", "deviceCode", 20));
        headList.add(new cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity("设备名称", "deviceName", 20));
        headList.add(new cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity("时间戳", "ts", 25));
        headList.add(new cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity("数值", "itemValue", 15));

        List<Map<String, Object>> dataList = new ArrayList<>();
        for (DeviceRealtimeVO vo : data) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("itemId", vo.getItemId());
            item.put("propName", vo.getPropName());
            item.put("deviceCode", vo.getDeviceCode());
            item.put("deviceName", vo.getDeviceName());
            item.put("ts", vo.getTs());
            item.put("itemValue", vo.getItemValue());
            dataList.add(item);
        }
        com.siact.common.utils.ExcelUtils.exportExcel(headList, "设备实时数据", dataList, response);
    }

    private void writeCsv(List<DeviceRealtimeVO> data, HttpServletResponse response) {
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment;filename="
                    + java.net.URLEncoder.encode("设备实时数据.csv", "UTF-8"));
            response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

            String[] headers = {"点位ID", "属性名称", "设备编码", "设备名称", "时间戳", "数值"};
            response.getOutputStream().write(String.join(",", headers).getBytes("UTF-8"));
            response.getOutputStream().write("\n".getBytes("UTF-8"));

            for (DeviceRealtimeVO vo : data) {
                String[] row = {escapeCsv(vo.getItemId()), escapeCsv(vo.getPropName()),
                        escapeCsv(vo.getDeviceCode()), escapeCsv(vo.getDeviceName()),
                        escapeCsv(vo.getTs()), vo.getItemValue() != null ? vo.getItemValue().toPlainString() : ""};
                response.getOutputStream().write(String.join(",", row).getBytes("UTF-8"));
                response.getOutputStream().write("\n".getBytes("UTF-8"));
            }
            response.getOutputStream().flush();
        } catch (Exception e) {
            throw new BizException("CSV导出失败: " + e.getMessage());
        }
    }

    private void writeJson(List<DeviceRealtimeVO> data, HttpServletResponse response) {
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.setHeader("Content-Disposition", "attachment;filename="
                    + java.net.URLEncoder.encode("设备实时数据.json", "UTF-8"));
            String json = com.siact.common.utils.JacksonUtils.toPrettyJson(data);
            response.getOutputStream().write(json.getBytes("UTF-8"));
            response.getOutputStream().flush();
        } catch (Exception e) {
            throw new BizException("JSON导出失败: " + e.getMessage());
        }
    }

    /**
     * 流式写入一批数据到 OutputStream（中等数据量用）
     */
    private void writeBatchToStream(List<DeviceRealtimeVO> batch, String format,
                                    java.io.OutputStream out, boolean isFirstBatch) throws Exception {
        if ("csv".equalsIgnoreCase(format)) {
            writeCsvBatchToStream(batch, out, isFirstBatch);
        } else if ("json".equalsIgnoreCase(format)) {
            writeJsonBatchToStream(batch, out, isFirstBatch);
        }
        // excel 格式不支持流式写入，中等数据量也会降级为全量内存方式
    }

    private void writeCsvBatchToStream(List<DeviceRealtimeVO> batch, java.io.OutputStream out,
                                       boolean isFirstBatch) throws Exception {
        if (isFirstBatch) {
            String[] headers = {"点位ID", "属性名称", "设备编码", "设备名称", "时间戳", "数值"};
            out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            out.write(String.join(",", headers).getBytes("UTF-8"));
            out.write("\n".getBytes("UTF-8"));
        }
        for (DeviceRealtimeVO vo : batch) {
            String[] row = {escapeCsv(vo.getItemId()), escapeCsv(vo.getPropName()),
                    escapeCsv(vo.getDeviceCode()), escapeCsv(vo.getDeviceName()),
                    escapeCsv(vo.getTs()), vo.getItemValue() != null ? vo.getItemValue().toPlainString() : ""};
            out.write(String.join(",", row).getBytes("UTF-8"));
            out.write("\n".getBytes("UTF-8"));
        }
        out.flush();
    }

    private void writeJsonBatchToStream(List<DeviceRealtimeVO> batch, java.io.OutputStream out,
                                        boolean isFirstBatch) throws Exception {
        byte[] jsonBytes = com.siact.common.utils.JacksonUtils.toJsonBytes(batch);
        if (isFirstBatch) {
            out.write("[".getBytes("UTF-8"));
        } else {
            out.write(",".getBytes("UTF-8"));
        }
        out.write(jsonBytes, 1, jsonBytes.length - 1); // 去掉开头的 [ 和结尾的 ]
        out.flush();
    }

    /**
     * 写入临时文件（大数据量用）
     */
    private void writeBatchToWriter(List<DeviceRealtimeVO> batch, String format,
                                    java.io.BufferedWriter writer, boolean isFirstBatch) throws Exception {
        if ("csv".equalsIgnoreCase(format)) {
            writeCsvBatchToWriter(batch, writer, isFirstBatch);
        } else if ("json".equalsIgnoreCase(format)) {
            writeJsonBatchToWriter(batch, writer, isFirstBatch);
        }
    }

    private void writeCsvBatchToWriter(List<DeviceRealtimeVO> batch, java.io.BufferedWriter writer,
                                       boolean isFirstBatch) throws Exception {
        if (isFirstBatch) {
            String[] headers = {"点位ID", "属性名称", "设备编码", "设备名称", "时间戳", "数值"};
            writer.write(String.join(",", headers));
            writer.newLine();
        }
        for (DeviceRealtimeVO vo : batch) {
            String[] row = {escapeCsv(vo.getItemId()), escapeCsv(vo.getPropName()),
                    escapeCsv(vo.getDeviceCode()), escapeCsv(vo.getDeviceName()),
                    escapeCsv(vo.getTs()), vo.getItemValue() != null ? vo.getItemValue().toPlainString() : ""};
            writer.write(String.join(",", row));
            writer.newLine();
        }
    }

    private void writeJsonBatchToWriter(List<DeviceRealtimeVO> batch, java.io.BufferedWriter writer,
                                        boolean isFirstBatch) throws Exception {
        if (isFirstBatch) {
            writer.write("[");
        } else {
            writer.write(",");
        }
        String json = com.siact.common.utils.JacksonUtils.toJson(batch);
        // 去掉首尾的 [ ]
        writer.write(json.substring(1, json.length() - 1));
    }

    // ========== 辅助方法 ==========

    private void setResponseHeaders(String format, HttpServletResponse response) {
        try {
            response.setCharacterEncoding("UTF-8");
            switch (format.toLowerCase()) {
                case "csv":
                    response.setContentType("text/csv");
                    response.setHeader("Content-Disposition", "attachment;filename="
                            + java.net.URLEncoder.encode("设备实时数据.csv", "UTF-8"));
                    break;
                case "json":
                    response.setContentType("application/json");
                    response.setHeader("Content-Disposition", "attachment;filename="
                            + java.net.URLEncoder.encode("设备实时数据.json", "UTF-8"));
                    break;
                default:
                    response.setContentType("application/vnd.ms-excel");
                    response.setHeader("Content-Disposition", "attachment;filename="
                            + java.net.URLEncoder.encode("设备实时数据.xlsx", "UTF-8"));
                    break;
            }
        } catch (Exception e) {
            throw new BizException("设置响应头失败: " + e.getMessage());
        }
    }

    private String getTempFileSuffix(String format) {
        switch (format.toLowerCase()) {
            case "csv": return ".csv";
            case "json": return ".json";
            default: return ".txt";
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
```

**注意：** 中等数据量的 Excel 导出也走 `exportSmall` 的全量内存方式（EasyPoi 不支持流式写入）。只有 CSV 和 JSON 真正支持流式。对于中等数据量（5万~50万）的 Excel，在 `exportMedium` 中应该降级调用 `exportSmall`。修正 `exportMedium` 方法中对 Excel 格式的处理：

在 `exportMedium` 方法开头添加 Excel 格式的降级判断：

```java
    private void exportMedium(List<String> propCodes, DeviceRealtimeQuery query,
                              String format, HttpServletResponse response, long total) {
        // Excel 不支持流式写入，降级为全量内存方式（5万~50万在内存中可行）
        if ("excel".equalsIgnoreCase(format)) {
            log.warn("Excel 格式不支持流式导出，使用全量内存方式");
            exportSmall(propCodes, query, format, response);
            return;
        }

        setResponseHeaders(format, response);
        try {
            List<DeviceRealtimeVO> batch;
            for (long offset = 0; offset < total; offset += BATCH_SIZE) {
                int limit = (int) Math.min(BATCH_SIZE, total - offset);
                String dataSql = buildDataSql(propCodes, query.getStartTime(), query.getEndTime(),
                        query.getTsUnit(), query.getCalcType(), offset, limit);
                batch = queryData(dataSql, propCodes);
                if (batch.isEmpty()) break;
                writeBatchToStream(batch, format, response.getOutputStream(), offset == 0);
            }
            response.getOutputStream().flush();
        } catch (Exception e) {
            throw new BizException("导出失败: " + e.getMessage());
        }
    }
```

同理 `exportLarge` 也需要对 Excel 格式降级：

```java
    private void exportLarge(List<String> propCodes, DeviceRealtimeQuery query,
                             String format, HttpServletResponse response, long total) {
        // Excel 不支持流式写入，>50万条 Excel 使用 SXSSFWorkbook
        if ("excel".equalsIgnoreCase(format)) {
            exportLargeExcel(propCodes, query, response, total);
            return;
        }
        // ... 原有的 CSV/JSON 临时文件逻辑不变
    }
```

添加 `exportLargeExcel` 方法：

```java
    /**
     * 大数据量 Excel 导出：使用 SXSSFWorkbook 流式写入
     */
    private void exportLargeExcel(List<String> propCodes, DeviceRealtimeQuery query,
                                  HttpServletResponse response, long total) {
        org.apache.poi.xssf.streaming.SXSSFWorkbook workbook = null;
        try {
            workbook = new org.apache.poi.xssf.streaming.SXSSFWorkbook(1000); // 内存中保留 1000 行
            org.apache.poi.xssf.streaming.SXSSFSheet sheet = workbook.createSheet("设备实时数据");

            // 表头
            org.apache.poi.xssf.streaming.SXSSFRow headerRow = sheet.createRow(0);
            String[] headers = {"点位ID", "属性名称", "设备编码", "设备名称", "时间戳", "数值"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // 分批查询写入
            int rowNum = 1;
            for (long offset = 0; offset < total; offset += BATCH_SIZE) {
                int limit = (int) Math.min(BATCH_SIZE, total - offset);
                String dataSql = buildDataSql(propCodes, query.getStartTime(), query.getEndTime(),
                        query.getTsUnit(), query.getCalcType(), offset, limit);
                List<DeviceRealtimeVO> batch = queryData(dataSql, propCodes);
                if (batch.isEmpty()) break;

                for (DeviceRealtimeVO vo : batch) {
                    org.apache.poi.xssf.streaming.SXSSFRow row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(vo.getItemId() != null ? vo.getItemId() : "");
                    row.createCell(1).setCellValue(vo.getPropName() != null ? vo.getPropName() : "");
                    row.createCell(2).setCellValue(vo.getDeviceCode() != null ? vo.getDeviceCode() : "");
                    row.createCell(3).setCellValue(vo.getDeviceName() != null ? vo.getDeviceName() : "");
                    row.createCell(4).setCellValue(vo.getTs() != null ? vo.getTs() : "");
                    row.createCell(5).setCellValue(vo.getItemValue() != null ? vo.getItemValue().toPlainString() : "");
                }
            }

            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename="
                    + java.net.URLEncoder.encode("设备实时数据.xlsx", "UTF-8"));

            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
        } catch (Exception e) {
            throw new BizException("Excel导出失败: " + e.getMessage());
        } finally {
            if (workbook != null) {
                try { workbook.close(); } catch (Exception ignored) {}
                try { workbook.dispose(); } catch (Exception ignored) {}
            }
        }
    }
```

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/siact/module/device/service/DeviceRealtimeService.java \
        src/main/java/com/siact/module/device/service/impl/DeviceRealtimeServiceImpl.java
git commit -m "feat(device): add DeviceRealtimeService with query and tiered export"
```

---

### Task 4: Controller

**Files:**
- Create: `src/main/java/com/siact/module/device/controller/DeviceRealtimeController.java`

- [ ] **Step 1: 创建 Controller**

```java
// src/main/java/com/siact/module/device/controller/DeviceRealtimeController.java
package com.siact.module.device.controller;

import com.siact.common.annotation.NoResponseAdvice;
import com.siact.common.vo.PageVO;
import com.siact.module.device.query.DeviceRealtimeQuery;
import com.siact.module.device.service.DeviceRealtimeService;
import com.siact.module.device.vo.DeviceRealtimeVO;
import com.siact.module.device.vo.SelectOptionVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

@Api(tags = "设备实时数据")
@RequiredArgsConstructor
@RestController
@RequestMapping("/device/realtime")
public class DeviceRealtimeController {
    private final DeviceRealtimeService service;

    @ApiOperation("点位ID下拉选项")
    @GetMapping("/itemIds")
    public List<SelectOptionVO> listItemIds() {
        return service.listItemIds();
    }

    @ApiOperation("设备名称下拉选项")
    @GetMapping("/deviceNames")
    public List<SelectOptionVO> listDeviceNames() {
        return service.listDeviceNames();
    }

    @ApiOperation("分页查询实时数据")
    @PostMapping("/query")
    public PageVO<DeviceRealtimeVO> query(@Valid @RequestBody DeviceRealtimeQuery query,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int pageSize) {
        return service.query(query, page, pageSize);
    }

    @NoResponseAdvice
    @ApiOperation("导出实时数据")
    @GetMapping("/export")
    public void export(DeviceRealtimeQuery query,
                       @RequestParam(defaultValue = "excel") String format,
                       HttpServletResponse response) {
        service.export(query, format, response);
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -pl . -q 2>&1 | head -50`

Expected: BUILD SUCCESS（无编译错误）

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/siact/module/device/controller/DeviceRealtimeController.java
git commit -m "feat(device): add DeviceRealtimeController with query and export endpoints"
```

---

### Task 5: 最终验证

- [ ] **Step 1: 全量编译**

Run: `mvn compile -q 2>&1 | tail -5`

Expected: BUILD SUCCESS

- [ ] **Step 2: 检查所有新增文件**

Run: `git diff --name-only HEAD~4`

Expected: 6 个新增文件（SelectOptionVO, DeviceRealtimeVO, DeviceRealtimeQuery, DeviceRealtimeService, DeviceRealtimeServiceImpl, DeviceRealtimeController）+ 2 个修改文件（DeviceMappingRepository, DeviceMappingRepositoryImpl）