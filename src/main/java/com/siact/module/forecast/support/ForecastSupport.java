package com.siact.module.forecast.support;

import com.siact.module.base.dto.BasicDataDTO;
import com.siact.module.base.dto.ColumnChartDTO;
import com.siact.sec.dto.CommonChartParamsDto;
import com.siact.sec.dto.IntervalDataDto;
import com.siact.sec.utils.CommonHandle;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author : kzuo
 * @version : 1.0
 * @date : 2026-02-25 15:36
 * @className : ForecastSupport
 * @description :  预测模块支撑类
 */
@Component
public class ForecastSupport {

    public Map<String, List<Object[]>> buildForecastValueMap(List<IntervalDataDto> dataDtoList, CommonChartParamsDto paramsDto) {
        ColumnChartDTO columnChartDTO = CommonHandle.buildColumnChartDTO(paramsDto, dataDtoList);
        List<BasicDataDTO> list = CollectionUtils.isNotEmpty(columnChartDTO.getData()) ? columnChartDTO.getData() : Collections.emptyList();
        return list.stream().collect(Collectors.toMap(BasicDataDTO::getDataCode, BasicDataDTO::getData, (v1, v2) -> v1));
    }
}
