package com.siact.hydrocore.sec.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


/**
 * 等时间间隔数据返回格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntervalDataDto {
    // 数据编码
    private String insDataCode;
    // 数据编码
    private String dataCode;
    // 数据名称
    private String dataName;
    // 单位
    private String unit;
    // 时间
    private String time;
    // 数据值
    private BigDecimal itemVal;
}
