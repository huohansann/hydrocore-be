package com.siact.module.snapshot.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "快照查询详情DTO")
public class SnapshotChartQueryDetailDTO {
    @ApiModelProperty("数据类型")
    private String type;

    @ApiModelProperty("图表查询code")
    private String code;

    @ApiModelProperty("数据编码")
    private List<String> dataCodeList;

    @ApiModelProperty("名称列表")
    private List<String> nameList;
}
