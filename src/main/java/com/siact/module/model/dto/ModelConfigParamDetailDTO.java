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
    @ApiModelProperty("参数类型")
    private String type;
    @ApiModelProperty("参数描述")
    private String name;
    @ApiModelProperty("字符串类参数值")
    private String value;
    @ApiModelProperty("是否选中(仅选择框使用)")
    private String selected;
    @ApiModelProperty("字符串子集(多选框)")
    private ArrayList<ModelConfigParamDetailDTO> paramList;

}
