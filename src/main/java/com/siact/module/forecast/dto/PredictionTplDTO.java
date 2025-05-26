package com.siact.module.forecast.dto;

import lombok.Data;

import java.util.List;

/**
 * @desc:
 * @author: zhangwentao
 * @create: 2025-05-26 14:29
 */
@Data
public class PredictionTplDTO {
    private String tabName;
    private String  tabCode;
    private List<AttributeParametersDTO> curve;
}