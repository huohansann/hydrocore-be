package com.siact.module.base.dto;


import lombok.Data;

import java.math.BigDecimal;


/**
 * 等时间间隔数据返回格式
 */
@Data
public class IntervalDataDto {

    private String dataCode;
    private String dataName;
    private String unit;
    private String time;
    private BigDecimal itemVal;

}
