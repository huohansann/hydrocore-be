package com.siact.module.level.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LevelAlgorithmResultVO {
    private BigDecimal levelTrend;
    private BigDecimal recommendedOpening;
    private String levelStatus;
}
