package com.siact.module.base.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel(description = "项目属性值")
@Data
public class ProjectPropDTO {

    @ApiModelProperty(value = "类型名称")
    private String type_name;
    private String type_code;
    private String type_unit;
    private List<Object[]> data;
}
