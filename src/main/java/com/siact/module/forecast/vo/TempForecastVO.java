package com.siact.module.forecast.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author : kzuo
 * @version : 1.0
 * @date : 2026-02-25 10:51
 * @className : TempForecastVO
 * @description : 温度预测数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TempForecastVO implements Serializable {
    private static final long serialVersionUID = 1L;

    List<String> xdata; // 时间轴
    List<TempForecastInfoVO> series; // 数据集
}
