package com.siact.module.level.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LevelRealtimeVO {
    private BigDecimal level;
    private BigDecimal opening;
    private String mode;
    private String levelStatus;
    private BigDecimal levelTrend;
    private BigDecimal recommendedOpening;
}
