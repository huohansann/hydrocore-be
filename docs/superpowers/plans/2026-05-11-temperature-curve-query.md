# 温度实际值+预测值曲线查询接口 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 新增 POST /forecast/queryActualAndForecast 接口，同时返回温度实际值和预测值曲线数据。

**Architecture:** 新建 Query DTO，扩展 TemperaturePredictRepository 添加查询方法，在 ForecastKilnService 中实现查询逻辑（参考 queryTemperature），在 Controller 新增端点。

**Tech Stack:** Java 8, Spring Boot, MyBatis-Plus, Swagger/Knife4j, TDengine

**Compile command:** `cd /home/Tso/devroot/code/projects/kic-be && export JAVA_HOME=/etc/jdk/zulu8 && mvn compile -s ~/.m2/settings-siact.xml -q 2>&1 | tail -10`

---

### Task 1: 新建 TempActualForecastQuery

**Files:**
- Create: `src/main/java/com/siact/module/forecast/query/TempActualForecastQuery.java`

- [ ] **Step 1: 创建 Query DTO**

```java
package com.siact.module.forecast.query;

import com.siact.common.validated.StringContains;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class TempActualForecastQuery {

    @NotEmpty(message = "属性数字化编码不能为空")
    private List<String> dataCodes;

    @NotBlank(message = "开始时间不能为空")
    private String startTime;

    @NotBlank(message = "结束时间不能为空")
    private String endTime;

    @NotNull(message = "步长不能为空")
    private Integer ts;

    @StringContains(limitValues = {"Y", "M", "D", "H", "MIN"}, message = "步长单位不正确")
    private String tsUnit;

    private String formatVal;

    @StringContains(limitValues = {"AVG", "MAX", "MIN", "LAST", "FIRST", "TOTAL", "INC", "SUM", "COUNT"}, message = "计算类型不正确")
    private String calcType;

    private List<String> names;
}
```

- [ ] **Step 2: 验证编译通过**

Run: `cd /home/Tso/devroot/code/projects/kic-be && export JAVA_HOME=/etc/jdk/zulu8 && mvn compile -s ~/.m2/settings-siact.xml -q 2>&1 | tail -10`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/siact/module/forecast/query/TempActualForecastQuery.java
git commit -m "feat(forecast): 新增 TempActualForecastQuery 查询参数"
```

---

### Task 2: 扩展 TemperaturePredictRepository 添加查询方法

**Files:**
- Modify: `src/main/java/com/siact/module/algorithm/repository/TemperaturePredictRepository.java`
- Modify: `src/main/java/com/siact/module/algorithm/repository/impl/TemperaturePredictRepositoryImpl.java`

- [ ] **Step 1: 在 Repository 接口添加方法声明**

将 `TemperaturePredictRepository.java` 内容改为：

```java
package com.siact.module.algorithm.repository;

import com.siact.common.repository.BaseRepository;
import com.siact.module.algorithm.entity.TemperaturePredictEntity;

import java.util.List;

public interface TemperaturePredictRepository extends BaseRepository<TemperaturePredictEntity> {

    List<TemperaturePredictEntity> queryByPropCodesAndTimeRange(List<String> propCodes, String startTime, String endTime);
}
```

- [ ] **Step 2: 在 RepositoryImpl 添加方法实现**

将 `TemperaturePredictRepositoryImpl.java` 内容改为：

```java
package com.siact.module.algorithm.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.siact.common.repository.BaseRepositoryImpl;
import com.siact.module.algorithm.mapper.TemperaturePredictMapper;
import com.siact.module.algorithm.entity.TemperaturePredictEntity;
import com.siact.module.algorithm.repository.TemperaturePredictRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class TemperaturePredictRepositoryImpl extends BaseRepositoryImpl<TemperaturePredictMapper, TemperaturePredictEntity> implements TemperaturePredictRepository {
    private final TemperaturePredictMapper mapper;

    @Override
    public List<TemperaturePredictEntity> queryByPropCodesAndTimeRange(List<String> propCodes, String startTime, String endTime) {
        if (CollectionUtils.isEmpty(propCodes)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<TemperaturePredictEntity> wrapper = Wrappers.<TemperaturePredictEntity>lambdaQuery()
                .in(TemperaturePredictEntity::getPropCode, propCodes)
                .ge(TemperaturePredictEntity::getTime, startTime)
                .le(TemperaturePredictEntity::getTime, endTime)
                .orderByAsc(TemperaturePredictEntity::getTime);
        return mapper.selectList(wrapper);
    }
}
```

- [ ] **Step 3: 验证编译通过**

Run: `cd /home/Tso/devroot/code/projects/kic-be && export JAVA_HOME=/etc/jdk/zulu8 && mvn compile -s ~/.m2/settings-siact.xml -q 2>&1 | tail -10`
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/siact/module/algorithm/repository/TemperaturePredictRepository.java src/main/java/com/siact/module/algorithm/repository/impl/TemperaturePredictRepositoryImpl.java
git commit -m "feat(algorithm): 扩展 TemperaturePredictRepository 添加按时间和编码查询方法"
```

---

### Task 3: ForecastKilnService 新增接口方法

**Files:**
- Modify: `src/main/java/com/siact/module/forecast/service/ForecastKilnService.java`

- [ ] **Step 1: 在接口中添加方法声明**

在 `ForecastKilnService.java` 文件末尾的 `}` 之前添加：

```java

    /**
     * 查询温度实际值与预测值曲线数据
     *
     * @param query 查询参数
     * @return 返回温度实际值与预测值数据
     */
    TempForecastVO queryActualAndForecast(TempActualForecastQuery query);
```

并在文件头部添加 import：

```java
import com.siact.module.forecast.query.TempActualForecastQuery;
```

- [ ] **Step 2: 验证编译通过（预期失败，因为实现类尚未实现方法）**

如果编译失败是正常的（接口方法未实现），直接进入 Task 4。

---

### Task 4: ForecastKilnServiceImpl 实现查询逻辑

**Files:**
- Modify: `src/main/java/com/siact/module/forecast/service/impl/ForecastKilnServiceImpl.java`

- [ ] **Step 1: 注入 TemperaturePredictRepository**

在类的字段区域（约第 72 行 `private @Resource ForecastSupport support;` 之后）添加：

```java
    private @Resource com.siact.module.algorithm.repository.TemperaturePredictRepository temperaturePredictRepository;
```

> 注意：该文件使用 `@Resource` 注入而非构造器注入（与文件现有风格一致）。

- [ ] **Step 2: 在文件末尾（`}` 之前）添加实现方法**

```java

    @Override
    public TempForecastVO queryActualAndForecast(TempActualForecastQuery query) {
        List<String> dataCodes = query.getDataCodes();
        List<String> names = query.getNames();

        // 1. 查询实际值（TDengine）
        IntervalValParamsDto dto = ConvertUtils.sourceToTarget(query, IntervalValParamsDto.class);
        List<IntervalDataDto> intervalDataDtos = taosDataService.queryIntervalVal(dto);
        Map<String, List<Object[]>> historyData = support.buildForecastValueMap(intervalDataDtos, ConvertUtils.sourceToTarget(dto, CommonChartParamsDto.class));

        // 2. 查询预测值（temperature_predict）
        List<TemperaturePredictEntity> predictEntities = temperaturePredictRepository.queryByPropCodesAndTimeRange(dataCodes, query.getStartTime(), query.getEndTime());
        Map<String, List<Object[]>> predictData = predictEntities.stream()
                .collect(Collectors.groupingBy(
                        TemperaturePredictEntity::getPropCode,
                        LinkedHashMap::new,
                        Collectors.mapping(e -> new Object[]{e.getTime(), e.getItemValue()}, Collectors.toList())
                ));

        // 3. 获取显示配置
        List<PredictionDataShowTplDTO> dataShowTplDTOList = tplService.getListByCode("kilnPredictionDataShow", PredictionDataShowTplDTO.class);
        Map<String, PredictionDataShowTplDTO> dataShowTplDTOMap = dataShowTplDTOList.stream().collect(Collectors.toMap(
                PredictionDataShowTplDTO::getDataCode, o -> o, (v1, v2) -> v1));

        // 4. 获取控制限配置
        Map<String, ControlIntervalConfigHisChartDataDTO> controlConfigMaps = controlIntervalConfigService.queryHistoryConfigChart(
                dataCodes, query.getStartTime(), query.getEndTime(), query.getTs(), query.getTsUnit(), query.getFormatVal());

        // 5. 生成时间轴
        List<String> xdata = IntervalTimeUtil.getIntervalTimeList(query.getStartTime(), query.getEndTime(), query.getTsUnit(), query.getTs(), query.getFormatVal());

        // 6. 组装结果
        List<TempForecastInfoVO> series = new ArrayList<>();
        for (int i = 0; i < dataCodes.size(); i++) {
            String dataCode = dataCodes.get(i);
            String dataName = CollectionUtils.isNotEmpty(names) ? names.get(i) : null;
            ControlIntervalConfigHisChartDataDTO hisChartDataDTO = controlConfigMaps.get(dataCode);

            PredictionDataShowTplDTO tpl = dataShowTplDTOMap.get(dataCode);
            HashMap<String, TempForecastInfoValueVO> dataMap = new HashMap<>();
            if (ObjectUtils.isNotEmpty(tpl)) {
                dataMap.put("dcs", TempForecastInfoValueVO.createIfMatch(tpl.getShowActual(), "运行值", historyData.get(dataCode)));
                dataMap.put("predict", TempForecastInfoValueVO.createIfMatch(true, "温度预测值", predictData.get(dataCode)));
                dataMap.put("upControl", TempForecastInfoValueVO.createIfMatch(tpl.getShowUpControl(), "上波动限", hisChartDataDTO.getUpControlChart()));
                dataMap.put("lowControl", TempForecastInfoValueVO.createIfMatch(tpl.getShowLowControl(), "下波动限", hisChartDataDTO.getLowControlChart()));
                dataMap.put("upAlarm", TempForecastInfoValueVO.createIfMatch(tpl.getShowUpAlarm(), "上告警限", hisChartDataDTO.getUpAlarmChart()));
                dataMap.put("lowAlarm", TempForecastInfoValueVO.createIfMatch(tpl.getShowLowAlarm(), "下告警限", hisChartDataDTO.getLowAlarmChart()));
                dataMap.put("temperatureSet", TempForecastInfoValueVO.createIfMatch(tpl.getShowTemperatureSet(), "温度设定值", hisChartDataDTO.getTemperatureSetChart()));
            }

            TempForecastInfoVO tempForecastInfoVO = TempForecastInfoVO.builder()
                    .dataCode(dataCode)
                    .name(StringUtils.isNotBlank(dataName) ? dataName + "趋势" : null)
                    .maxUpControlVal(NumberUtils.createBigDecimal(hisChartDataDTO.getMaxUpControlVal()))
                    .minLowControlVal(NumberUtils.createBigDecimal(hisChartDataDTO.getMinLowControlVal()))
                    .maxUpAlarmVal(NumberUtils.createBigDecimal(hisChartDataDTO.getMaxUpAlarmVal()))
                    .minLowAlarmVal(NumberUtils.createBigDecimal(hisChartDataDTO.getMinLowAlarmVal()))
                    .maxTemperatureSetVal(NumberUtils.createBigDecimal(hisChartDataDTO.getMaxTemperatureSetVal()))
                    .minTemperatureSetVal(NumberUtils.createBigDecimal(hisChartDataDTO.getMinTemperatureSetVal()))
                    .data(dataMap)
                    .build();

            series.add(tempForecastInfoVO);
        }

        return TempForecastVO.builder().xdata(xdata).series(series).build();
    }
```

需要在文件头部添加的 import：

```java
import com.siact.module.algorithm.entity.TemperaturePredictEntity;
import com.siact.module.forecast.query.TempActualForecastQuery;
```

- [ ] **Step 3: 验证编译通过**

Run: `cd /home/Tso/devroot/code/projects/kic-be && export JAVA_HOME=/etc/jdk/zulu8 && mvn compile -s ~/.m2/settings-siact.xml -q 2>&1 | tail -10`
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/siact/module/forecast/service/ForecastKilnService.java src/main/java/com/siact/module/forecast/service/impl/ForecastKilnServiceImpl.java
git commit -m "feat(forecast): 实现 queryActualAndForecast 温度实际值与预测值曲线查询"
```

---

### Task 5: ForecastKilnController 新增端点

**Files:**
- Modify: `src/main/java/com/siact/module/forecast/controller/ForecastKilnController.java`

- [ ] **Step 1: 在 Controller 中添加新端点**

在 `ForecastKilnController.java` 文件的最后一个方法 `queryKilnForecastInfo` 之后，类的 `}` 之前添加：

```java

    @ApiOperationSupport(order = 60)
    @ApiOperation("查询温度实际值与预测值曲线")
    @PostMapping("/queryActualAndForecast")
    public TempForecastVO queryActualAndForecast(@RequestBody @Validated TempActualForecastQuery query) {
        return service.queryActualAndForecast(query);
    }
```

在文件头部添加 import：

```java
import com.siact.module.forecast.query.TempActualForecastQuery;
```

- [ ] **Step 2: 验证编译通过**

Run: `cd /home/Tso/devroot/code/projects/kic-be && export JAVA_HOME=/etc/jdk/zulu8 && mvn compile -s ~/.m2/settings-siact.xml -q 2>&1 | tail -10`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/siact/module/forecast/controller/ForecastKilnController.java
git commit -m "feat(forecast): 新增 queryActualAndForecast 查询端点"
```