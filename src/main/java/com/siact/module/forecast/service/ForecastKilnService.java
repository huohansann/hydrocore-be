package com.siact.module.forecast.service;

import com.siact.module.forecast.dto.ForecastKilnParamsDTO;
import com.siact.module.forecast.vo.KilnForecastLineChartVO;
import com.siact.module.forecast.vo.ForecastKilnMenuVO;
import com.siact.module.forecast.vo.LineChartVO;
import com.siact.sec.dto.CommonChartParamsDto;

import java.util.List;

public interface ForecastKilnService {
    /**
     * 查询菜单信息
     *
     * @return
     */
    List<ForecastKilnMenuVO> queryForecastKilnMenu(String tpl);


    /**
     * 查询窑炉参数信息
     *
     * @param dto
     * @return
     */
    List<LineChartVO> queryForecastInfo(ForecastKilnParamsDTO dto);


    /**
     * 获取预测信息
     *
     * @param dto
     * @return
     */
    List<LineChartVO> queryKilnForecastInfo(ForecastKilnParamsDTO dto);
}
