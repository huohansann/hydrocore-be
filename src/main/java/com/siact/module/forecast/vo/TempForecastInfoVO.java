package com.siact.module.forecast.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/**
 * @author : kzuo
 * @version : 1.0
 * @date : 2026-02-25 10:53
 * @className : TempForecastInfoVO
 * @description : 温度预测详情数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TempForecastInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String dataCode;                            // 孪生编码
    private String name;                                // 名称
    private Map<String, TempForecastInfoValueVO> data;  // 点位数据
    private BigDecimal maxUpControlVal;                 // 上控制限
    private BigDecimal minLowControlVal;                // 下控制限
    private BigDecimal maxUpAlarmVal;                   // 上告警限
    private BigDecimal minLowAlarmVal;                  // 下告警限
    private BigDecimal maxTemperatureSetVal;            // 温度设定值(max)
    private BigDecimal minTemperatureSetVal;            // 温度设定值(min)
}
