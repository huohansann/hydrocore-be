package com.siact.module.base.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmPointVO {
    private String pointName;
    private String dataCode;
    private BigDecimal currentValue;
    private String limitType;
    private BigDecimal limitValue;
}
