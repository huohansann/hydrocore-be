package com.siact.module.levelcontrol.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LevelAlgorithmResultVO {
    private BigDecimal levelTrend;
    private BigDecimal recommendedOpening;
    private String levelStatus;
}
