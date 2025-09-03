package com.siact.module.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;

@Data
@ApiModel("参数设置")
public class ModelConfigParamDetailDTO {
    @ApiModelProperty("算法类型")
    private String type;
    @ApiModelProperty("参数编码")
    private String paramCode;
    @ApiModelProperty("参数类型,text/int/float")
    private String paramType;
    @ApiModelProperty("参数名称")
    private String name;
    @ApiModelProperty("参数描述")
    private String describe;
    @ApiModelProperty("参数描述,说明参数的类型,浮点/整数等")
    private String message;
    @ApiModelProperty("参数值,可能为字符串或int或浮点")
    private Object value;
    @ApiModelProperty("是否选中(仅选择框使用)")
    private Boolean selected;
    @ApiModelProperty("字符串子集(多选框)")
    private ArrayList<ModelConfigParamDetailDTO> paramList;

}
