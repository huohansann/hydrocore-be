package com.siact.module.snapshot.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "快照查询DTO")
public class SnapshotChartQueryDTO {


    @ApiModelProperty("查询数据")
    private List<SnapshotChartQueryDetailDTO> queryList;

    @ApiModelProperty("开始时间")
    private String startTime;

    @ApiModelProperty("结束时间")
    private String endTime;

    @ApiModelProperty("时间间隔")
    private Integer ts;

    @ApiModelProperty("时间间隔单位")
    private String tsUnit;

    @ApiModelProperty("时间格式")
    private String formatVal;


}
