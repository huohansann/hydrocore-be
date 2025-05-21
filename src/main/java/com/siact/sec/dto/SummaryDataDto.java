package com.siact.sec.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class SummaryDataDto {


    private String dataCode;
    /**
     * 量值
     */
    private BigDecimal itemValue;
    /**
     * 费值
     */
    private BigDecimal itemFee;

    private String type;
    /**
     * 时间
     */
    private String time;
}
