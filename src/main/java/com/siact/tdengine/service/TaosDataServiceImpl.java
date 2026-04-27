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

    // ========== 原始时序数据查询 ==========

    @Override
    public List<Map<String, Object>> queryRawData(List<String> dataCodes, String startTime, String endTime) {
        log.info("查询原始时序数据, dataCodes数量: {}, startTime: {}, endTime: {}", dataCodes.size(), startTime, endTime);

        List<Map<String, Object>> results = new ArrayList<>();

        if (dataCodes == null || dataCodes.isEmpty()) {
            log.error("原始时序数据查询参数为空");
            return results;
        }

        try {
            String sql = TaosSqlBuilder.buildRawDataQuerySql(dataCodes, startTime, endTime);
            results = jdbcClient.executeQuery(sql, rs -> {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("ts", formatTimestamp(jdbcClient.getString(rs, "ts")));
                row.put("datacode", jdbcClient.getString(rs, "datacode"));
                Double value = jdbcClient.getDouble(rs, "itemvalue");
                row.put("itemvalue", value != null ? BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP) : null);
                return row;
            });
        } catch (Exception e) {
            log.error("查询原始时序数据失败: {}", e.getMessage(), e);
        }

        return results;
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
}
