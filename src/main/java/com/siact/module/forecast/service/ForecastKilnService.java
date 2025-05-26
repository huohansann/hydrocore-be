package com.siact.module.forecast.service;

import com.siact.module.forecast.dto.ForecastKilnParamsDTO;
import com.siact.module.forecast.dto.PredictionTplDTO;
import com.siact.module.forecast.vo.ForecastKilnDetailVO;
import com.siact.module.forecast.vo.ForecastKilnLineChartVO;
import com.siact.module.forecast.vo.ForecastKilnMenuVO;

import java.util.List;

public interface ForecastKilnService {
    /**
     * 查询菜单信息
     * @return
     */
    List<ForecastKilnMenuVO> queryForecastKilnMenu(String tpl);

    /**
     * 查询窑炉预测信息
     * @param dto
     * @return
     */
    ForecastKilnLineChartVO queryForecastInfo(ForecastKilnParamsDTO dto);
}
