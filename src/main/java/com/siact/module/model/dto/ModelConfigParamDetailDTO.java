package com.siact.module.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;

@Data
@ApiModel("参数设置")
public class ModelConfigParamDetailDTO {
    @ApiModelProperty("参数编码")
    private String paramCode;
    @ApiModelProperty("参数类型 0:字符串(输入框) 1:数组List(多选框)")
    private String type;
    @ApiModelProperty("参数描述")
    private String label;
    @ApiModelProperty("字符串类参数值")
    private String value;
    @ApiModelProperty("字符串子集(多选框)")
    private ArrayList<ModelConfigParamDetailDTO> children;

}
