package com.siact.hydrocore.sec.service;

import com.siact.hydrocore.sec.dto.CommonChartResultDto;
import com.siact.hydrocore.sec.vo.CommonChartParamsVo;

public interface DataChartService {
    CommonChartResultDto queryCommonChartData(CommonChartParamsVo vo);
}
