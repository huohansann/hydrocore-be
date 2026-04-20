package com.siact.module.device.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "下拉选项")
public class SelectOptionVO {

    @ApiModelProperty("显示文本")
    private String label;

    @ApiModelProperty("实际值")
    private String value;
}