package com.siact.module.forecast.service;

import com.siact.module.forecast.dto.ForecastKilnParamsDTO;
import com.siact.module.forecast.query.TempActualForecastQuery;
import com.siact.module.forecast.query.TempForecastQuery;
import com.siact.module.forecast.vo.ForecastKilnMenuVO;
import com.siact.module.forecast.vo.LineChartVO;
import com.siact.module.forecast.vo.TempForecastVO;

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


    /**
     * 根据查询参数获取窑炉温度历史数据和单步/多步预测数据
     *
     * @param query 查询参数
     * @return 返回温度预测结果
     */
    TempForecastVO queryTemperature(TempForecastQuery query);

    /**
     * 查询温度实际值与预测值曲线数据
     *
     * @param query 查询参数
     * @return 返回温度实际值与预测值数据
     */
    TempForecastVO queryActualAndForecast(TempActualForecastQuery query);
}
