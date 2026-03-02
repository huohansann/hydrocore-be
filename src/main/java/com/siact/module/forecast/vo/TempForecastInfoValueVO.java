package com.siact.module.forecast.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author : kzuo
 * @version : 1.0
 * @date : 2026-02-25 11:03
 * @className : TempForecastInfoValueVO
 * @description : 温度预测项数据传输对象
 */
@Data
@AllArgsConstructor
public class TempForecastInfoValueVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private List<Object[]> value;

    public static TempForecastInfoValueVO createIfMatch(boolean condition, String name, List<Object[]> value) {
        return condition ? new TempForecastInfoValueVO(name, value) : null;
    }
}
