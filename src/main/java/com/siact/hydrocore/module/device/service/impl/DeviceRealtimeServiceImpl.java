package com.siact.hydrocore.module.device.service.impl;

import com.siact.hydrocore.common.exception.BizException;
import com.siact.hydrocore.common.utils.JacksonUtils;
import com.siact.hydrocore.common.vo.PageVO;
import com.siact.hydrocore.module.device.entity.DeviceMappingEntity;
import com.siact.hydrocore.module.device.query.DeviceRealtimeQuery;
import com.siact.hydrocore.module.device.repository.DeviceMappingRepository;
import com.siact.hydrocore.module.device.service.DeviceRealtimeService;
import com.siact.hydrocore.module.device.vo.DeviceRealtimeVO;
import com.siact.hydrocore.module.device.vo.SelectOptionVO;
import com.siact.hydrocore.tdengine.util.TaosJdbcClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class DeviceRealtimeServiceImpl implements DeviceRealtimeService {

    private final DeviceMappingRepository deviceMappingRepository;
    private final TaosJdbcClient jdbcClient;

    private static final int BATCH_SIZE = 10_000;
    private static final int MAX_QUERY_DAYS = 7;
    private static final int DEFAULT_START_DAYS_AGO = 1;
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

    @Override
    public PageVO<DeviceRealtimeVO> query(DeviceRealtimeQuery query, int page, int pageSize) {
        normalizeAndValidateTimeRange(query);

        List<String> propCodes = deviceMappingRepository.findPropCodesByConditions(
                query.getItemIds(), query.getPropName(), query.getDeviceCodes());
        if (propCodes.isEmpty()) {
            return PageVO.empty();
        }

        String countSql = buildCountSql(propCodes, query.getStartTime(), query.getEndTime());
        Long total = queryCount(countSql);
        if (total == 0) {
            return PageVO.empty();
        }

        long offset = (long) (page - 1) * pageSize;
        String dataSql = buildDataSql(propCodes, query.getStartTime(), query.getEndTime(), offset, pageSize);
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

    @Override
    public void export(DeviceRealtimeQuery query, String format, HttpServletResponse response) {
        normalizeAndValidateTimeRange(query);

        List<String> propCodes = deviceMappingRepository.findPropCodesByConditions(
                query.getItemIds(), query.getPropName(), query.getDeviceCodes());
        if (propCodes.isEmpty()) {
            throw new BizException("没有匹配的点位数据");
        }

        String countSql = buildCountSql(propCodes, query.getStartTime(), query.getEndTime());
        Long total = queryCount(countSql);
        if (total == 0) {
            throw new BizException("没有查询到数据");
        }

        if (!"csv".equalsIgnoreCase(format) && !"json".equalsIgnoreCase(format)) {
            throw new BizException("不支持的导出格式: " + format + "，仅支持 csv/json");
        }

        log.info("导出数据量: {}, 使用格式: {}", total, format);
        setResponseHeaders(format, response);

        try {
            for (long offset = 0; offset < total; offset += BATCH_SIZE) {
                int limit = (int) Math.min(BATCH_SIZE, total - offset);
                String dataSql = buildDataSql(propCodes, query.getStartTime(), query.getEndTime(), offset, limit);
                List<DeviceRealtimeVO> batch = queryData(dataSql, propCodes);
                if (batch.isEmpty()) break;
                writeBatchToStream(batch, format, response.getOutputStream(), offset == 0);
            }
            response.getOutputStream().flush();
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("导出失败: " + e.getMessage());
        }
    }

    // ========== SQL 构建 ==========

    private String buildCountSql(List<String> propCodes, String startTime, String endTime) {
        return "SELECT COUNT(*) FROM " + buildFromWhere(propCodes, startTime, endTime);
    }

    private String buildDataSql(List<String> propCodes, String startTime, String endTime,
                                long offset, int limit) {
        return "SELECT ts, devproperty as datacode, itemvalue FROM " + buildFromWhere(propCodes, startTime, endTime)
                + " ORDER BY ts DESC LIMIT " + limit + " OFFSET " + offset;
    }

    private String buildFromWhere(List<String> propCodes, String startTime, String endTime) {
        String inClause = propCodes.stream()
                .map(v -> "'" + v.replace("'", "''") + "'")
                .collect(Collectors.joining(", "));
        StringBuilder sb = new StringBuilder("datasource WHERE devproperty IN (")
                .append(inClause).append(")");
        if (StringUtils.isNotBlank(startTime)) {
            sb.append(" AND ts >= '").append(startTime).append("'");
        }
        if (StringUtils.isNotBlank(endTime)) {
            sb.append(" AND ts <= '").append(endTime).append("'");
        }
        return sb.append(" ").toString();
    }

    // ========== 查询执行 ==========

    private Long queryCount(String sql) {
        try {
            Long result = jdbcClient.executeQueryOne(sql, rs -> rs.getLong(1));
            return result != null ? result : 0L;
        } catch (Exception e) {
            log.error("COUNT 查询失败: {}", e.getMessage(), e);
            return 0L;
        }
    }

    private List<DeviceRealtimeVO> queryData(String sql, List<String> propCodes) {
        Map<String, DeviceMappingEntity> mappingMap = deviceMappingRepository.findByPropCodes(propCodes).stream()
                .collect(Collectors.toMap(DeviceMappingEntity::getPropCode, e -> e, (a, b) -> a));

        return jdbcClient.executeQuery(sql, rs -> {
            DeviceRealtimeVO vo = new DeviceRealtimeVO();
            String dataCode = jdbcClient.getString(rs, "datacode");
            vo.setTs(formatTimestamp(jdbcClient.getString(rs, "ts")));
            Double value = jdbcClient.getDouble(rs, "itemvalue");
            vo.setItemValue(value != null ? BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP) : null);

            DeviceMappingEntity mapping = mappingMap.get(dataCode);
            if (mapping != null) {
                vo.setItemId(mapping.getItemId());
                vo.setPropName(mapping.getPropName());
                vo.setPropCode(mapping.getPropCode());
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

    private void writeBatchToStream(List<DeviceRealtimeVO> batch, String format,
                                    java.io.OutputStream out, boolean isFirstBatch) throws Exception {
        if ("csv".equalsIgnoreCase(format)) {
            writeCsvBatchToStream(batch, out, isFirstBatch);
        } else {
            writeJsonBatchToStream(batch, out, isFirstBatch);
        }
    }

    private void writeCsvBatchToStream(List<DeviceRealtimeVO> batch, java.io.OutputStream out,
                                       boolean isFirstBatch) throws Exception {
        if (isFirstBatch) {
            out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            out.write(String.join(",", "点位ID", "属性名称", "属性编码", "设备名称", "时间戳", "数值")
                    .getBytes(StandardCharsets.UTF_8));
            out.write("\n".getBytes(StandardCharsets.UTF_8));
        }
        for (DeviceRealtimeVO vo : batch) {
            String[] row = {escapeCsv(vo.getItemId()), escapeCsv(vo.getPropName()),
                    escapeCsv(vo.getPropCode()), escapeCsv(vo.getDeviceName()),
                    escapeCsv(vo.getTs()), vo.getItemValue() != null ? vo.getItemValue().toPlainString() : ""};
            out.write(String.join(",", row).getBytes(StandardCharsets.UTF_8));
            out.write("\n".getBytes(StandardCharsets.UTF_8));
        }
        out.flush();
    }

    private void writeJsonBatchToStream(List<DeviceRealtimeVO> batch, java.io.OutputStream out,
                                        boolean isFirstBatch) throws Exception {
        byte[] jsonBytes = JacksonUtils.toJsonBytes(batch);
        if (isFirstBatch) {
            out.write("[".getBytes(StandardCharsets.UTF_8));
        } else {
            out.write(",".getBytes(StandardCharsets.UTF_8));
        }
        out.write(jsonBytes, 1, jsonBytes.length - 1);
        out.flush();
    }

    // ========== 辅助方法 ==========

    private void normalizeAndValidateTimeRange(DeviceRealtimeQuery query) {
        if (StringUtils.isBlank(query.getStartTime())) {
            query.setStartTime(LocalDateTime.now().minusDays(DEFAULT_START_DAYS_AGO)
                    .withHour(0).withMinute(0).withSecond(0).format(DT_FMT));
        }
        if (StringUtils.isBlank(query.getEndTime())) {
            query.setEndTime(LocalDateTime.now().format(DT_FMT));
        }

        LocalDateTime start = LocalDateTime.parse(query.getStartTime(), DT_FMT);
        LocalDateTime end = LocalDateTime.parse(query.getEndTime(), DT_FMT);

        if (start.isAfter(end)) {
            throw new BizException("开始时间不能大于结束时间");
        }
        if (ChronoUnit.DAYS.between(start, end) > MAX_QUERY_DAYS) {
            throw new BizException("查询时间范围不能超过" + MAX_QUERY_DAYS + "天");
        }
    }

    private void setResponseHeaders(String format, HttpServletResponse response) {
        try {
            response.setCharacterEncoding("UTF-8");
            if ("json".equalsIgnoreCase(format)) {
                response.setContentType("application/json");
                response.setHeader("Content-Disposition", "attachment;filename="
                        + URLEncoder.encode("设备实时数据.json", "UTF-8"));
            } else {
                response.setContentType("text/csv");
                response.setHeader("Content-Disposition", "attachment;filename="
                        + URLEncoder.encode("设备实时数据.csv", "UTF-8"));
            }
        } catch (Exception e) {
            throw new BizException("设置响应头失败: " + e.getMessage());
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
