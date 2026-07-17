package com.siact.hydrocore.sec.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CumulativeDataDTO {
    public CumulativeDataDTO() {}

    public CumulativeDataDTO(String code, BigDecimal value) {
        this.code = code;
        this.value = value;
    }

    @ApiModelProperty(value = "编码")
    private String code;
    @ApiModelProperty(value = "累计值")
    private BigDecimal value;
    @ApiModelProperty(value = "同比值")
    private BigDecimal yoy;
    @ApiModelProperty(value = "同比趋势")
    private String yoyTrend;
    @ApiModelProperty(value = "环比值")
    private BigDecimal qoq;
    @ApiModelProperty(value = "环比趋势")
    private String qoqTrend;
}
