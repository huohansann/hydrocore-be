package com.siact.sec.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CommonChartDataDto {

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "数字孪生dataCode")
    private String dataCode;

    @ApiModelProperty(value = "数据")
    private List<Object[]> data;

    @ApiModelProperty(value = "累加值")
    private BigDecimal totalValue;

    @ApiModelProperty(value = "平均值")
    private BigDecimal aveValue;

    @ApiModelProperty(value = "单位")
    private String unit;

    @ApiModelProperty(value = "是否展示表格")
    private boolean showTable;
}
