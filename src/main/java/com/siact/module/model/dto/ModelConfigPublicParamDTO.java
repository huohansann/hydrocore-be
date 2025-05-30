package com.siact.module.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("参数设置-公共参数")
public class ModelConfigPublicParamDTO {
    @ApiModelProperty("参数编码")
    private String paramCode;
    @ApiModelProperty("参数类型 0:字符串 1:数组List")
    private String type;
    @ApiModelProperty("字符串类参数值")
    private String value;
    @ApiModelProperty("列表类参数值")
    private List<String> listValue;
}
