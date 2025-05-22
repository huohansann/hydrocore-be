package com.siact.module.base.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class KilnInfoTotalWindDTO extends KilnInfoBase {
    /** 总气量 */
    private BigDecimal totalWindVal;
}
