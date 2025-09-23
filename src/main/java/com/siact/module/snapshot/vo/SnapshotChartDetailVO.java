package com.siact.module.snapshot.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class SnapshotChartDetailVO {
    @ApiModelProperty("图表类型")
    private String type;
    @ApiModelProperty("图表查询code")
    private String code;
    @ApiModelProperty("当前线段名称")
    private String name;
    @ApiModelProperty("当前线段数据编码")
    private String dataCode;
    @ApiModelProperty("当前线段数据")
    private List<Object[]> data;
}
