package com.siact.module.base.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FeeModelDataDTO {
    private String style;
    private String start;
    private String end;
    private String computeMark;
    private BigDecimal dataVal;
}
