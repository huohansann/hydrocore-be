package com.siact.module.control.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author : kzuo
 * @version : 1.0
 * @date : 2026-03-26 10:27
 * @className : GasForecastDataVO
 * @description : 天然气预测数据视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GasForecastDataVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private GasForecastDataValueVO dcs;
    private GasForecastDataValueVO forecast;
}
