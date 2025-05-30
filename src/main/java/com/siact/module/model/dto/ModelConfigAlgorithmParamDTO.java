package com.siact.module.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("参数设置-公共参数")
public class ModelConfigAlgorithmParamDTO {
    @ApiModelProperty("参数编码")
    private String paramCode;
    @ApiModelProperty("参数值")
    private String value;
    @ApiModelProperty("子项参数值")
    private List<ModelConfigAlgorithmParamDTO> dataList;
}
