package com.siact.hydrocore.sec.service.impl;

import com.siact.hydrocore.sec.dto.CommonChartResultDto;
import com.siact.hydrocore.sec.dto.IntervalDataDto;
import com.siact.hydrocore.sec.vo.CommonChartParamsVo;
import com.siact.hydrocore.tdengine.util.TaosJdbcClient;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DataChartServiceImplTest {

    @Test
    void returnsEmptyChartWhenTaosHasNoRows() {
        TaosJdbcClient jdbcClient = mock(TaosJdbcClient.class);
        when(jdbcClient.<IntervalDataDto>executeQuery(anyString(), any())).thenReturn(Collections.emptyList());
        DataChartServiceImpl service = new DataChartServiceImpl(jdbcClient);

        CommonChartResultDto result = service.queryCommonChartData(query());

        assertThat(result.getList()).isEmpty();
        assertThat(result.getXAxisData()).isEmpty();
    }

    private CommonChartParamsVo query() {
        CommonChartParamsVo vo = new CommonChartParamsVo();
        vo.setDataCodes(Collections.singletonList("test"));
        vo.setNames(Collections.singletonList("test"));
        vo.setStartTime("2026-07-21 00:00:00");
        vo.setEndTime("2026-07-21 01:00:00");
        vo.setTs(1);
        vo.setTsUnit("m");
        vo.setFormatVal("yyyy-MM-dd HH:mm:ss");
        vo.setCalcType("avg");
        return vo;
    }
}
