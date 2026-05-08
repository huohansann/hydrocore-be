package com.siact.module.levelcontrol.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LevelControlConfigVO {
    private String dataCode;
    private String mode;
    private BigDecimal aiPredictWindow;
    private BigDecimal aiPredictDuration;
    private BigDecimal pidPb;
    private BigDecimal pidTi;
    private BigDecimal pidTd;
    private BigDecimal manualControlValue;
    private BigDecimal safeLimit;
    private BigDecimal openingUpperLimit;
}
