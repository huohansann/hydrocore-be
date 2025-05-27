package com.siact.module.forecast.service;

import com.siact.module.forecast.dto.ForecastKilnParamsDTO;
import com.siact.module.forecast.vo.KilnForecastLineChartVO;
import com.siact.module.forecast.vo.ForecastKilnMenuVO;
import com.siact.module.forecast.vo.KilnLineChartVO;
import com.siact.module.forecast.vo.LineChartVO;
import com.siact.sec.dto.CommonChartParamsDto;
import com.siact.sec.vo.CommonChartParamsVo;

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
    KilnLineChartVO queryForecastInfo(ForecastKilnParamsDTO dto);

    /**
     * 查询窑炉预测信息
     * @param dto
     * @return
     */
    KilnForecastLineChartVO queryKilnForecastInfo(CommonChartParamsDto dto);

    List<LineChartVO> queryForecastInfo1(ForecastKilnParamsDTO dto);
}
