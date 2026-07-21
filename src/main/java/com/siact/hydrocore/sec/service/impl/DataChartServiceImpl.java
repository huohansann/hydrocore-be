package com.siact.hydrocore.sec.service.impl;

import com.siact.hydrocore.sec.dto.CommonChartParamsDto;
import com.siact.hydrocore.sec.dto.CommonChartResultDto;
import com.siact.hydrocore.sec.dto.IntervalDataDto;
import com.siact.hydrocore.sec.service.DataChartService;
import com.siact.hydrocore.sec.utils.CommonHandle;
import com.siact.hydrocore.sec.vo.CommonChartParamsVo;
import com.siact.hydrocore.tdengine.util.TaosJdbcClient;
import com.siact.hydrocore.tdengine.util.TaosSqlBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Slf4j
@RequiredArgsConstructor
@Service
public class DataChartServiceImpl implements DataChartService {

    private final TaosJdbcClient jdbcClient;

    @Override
    public CommonChartResultDto queryCommonChartData(CommonChartParamsVo vo) {
        String sql = TaosSqlBuilder.buildIntervalQuerySql(
                vo.getDataCodes(),
                vo.getStartTime(),
                vo.getEndTime(),
                vo.getTs(),
                normalizeTsUnit(vo.getTsUnit()),
                normalizeCalcType(vo.getCalcType())
        );

        List<IntervalDataDto> rows = jdbcClient.executeQuery(sql, rs -> {
            IntervalDataDto dto = new IntervalDataDto();
            String dataCode = jdbcClient.getString(rs, "datacode");
            dto.setInsDataCode(dataCode);
            dto.setDataCode(dataCode);
            dto.setTime(formatTimestamp(jdbcClient.getString(rs, "ts")));
            Double value = jdbcClient.getDouble(rs, "itemvalue");
            dto.setItemVal(value == null ? null : BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP));
            return dto;
        });

        if (rows.isEmpty()) {
            log.debug("No chart data found for dataCodes: {}", vo.getDataCodes());
            return emptyResult();
        }

        return CommonHandle.getCommonChartResultDto(toChartParams(vo), rows);
    }

    private CommonChartParamsDto toChartParams(CommonChartParamsVo vo) {
        CommonChartParamsDto dto = new CommonChartParamsDto();
        dto.setDataCodes(vo.getDataCodes());
        dto.setPropModelCodes(vo.getPropModelCodes());
        dto.setStartTime(vo.getStartTime());
        dto.setEndTime(vo.getEndTime());
        dto.setTs(vo.getTs());
        dto.setTsUnit(normalizeTsUnit(vo.getTsUnit()));
        dto.setCalcType(normalizeCalcType(vo.getCalcType()));
        dto.setFormatVal(vo.getFormatVal());
        dto.setNames(vo.getNames());
        dto.setUnits(vo.getUnits());
        dto.setShowTables(vo.getShowTables());
        return dto;
    }

    private CommonChartResultDto emptyResult() {
        CommonChartResultDto dto = new CommonChartResultDto();
        dto.setList(Collections.emptyList());
        dto.setXAxisData(Collections.emptyList());
        return dto;
    }

    private String normalizeTsUnit(String tsUnit) {
        if ("m".equals(tsUnit)) {
            return "MIN";
        }
        return StringUtils.defaultString(tsUnit).toUpperCase(Locale.ROOT);
    }

    private String normalizeCalcType(String calcType) {
        return StringUtils.defaultString(calcType, "AVG").toUpperCase(Locale.ROOT);
    }

    private String formatTimestamp(String ts) {
        if (StringUtils.isBlank(ts)) {
            return null;
        }
        if (ts.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.*")) {
            return ts.substring(0, 19);
        }
        return ts;
    }
}
