package com.siact.module.predicted.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("预测数据DTO")
public class PredictedDataDTO {
    @ApiModelProperty("dataCode")
    private String dataCode;
    @ApiModelProperty("预测类型 1:单步 2:多步")
    private Integer predictedType;
    @ApiModelProperty("预测类型Code,单步如:T20,T40,多步:MULTI")
    private Integer predictedTypeCode;
    @ApiModelProperty("数据时间 格式:yyyy-MM-dd HH:mm:ss")
    private String time;
    @ApiModelProperty("数据值")
    private String itemVal;
    @ApiModelProperty("数据单位")
    private String unit;
    @ApiModelProperty("数据集成时间")
    private String createTime;
}
