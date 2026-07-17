package com.siact.hydrocore.module.base.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@ApiModel(description = "孪生属性DTO")
@NoArgsConstructor
@AllArgsConstructor
public class SecPropBaseDTO {
    @ApiModelProperty(value = "属性id", position = 1)
    private Long id;

    @ApiModelProperty(value = "模型id", position = 2)
    private Long modelId;
    @ApiModelProperty(value = "模型DataCode", position = 3)
    private String modelDataCode;

    @ApiModelProperty(value = "实例id", position = 4)
    private Long insId;
    @ApiModelProperty(value = "实例DataCode", position = 5)
    private String insDataCode;

    @ApiModelProperty("base:基础属性,dyprop:动态属性,stprop:静态属性")
    private String type;

    @ApiModelProperty(value = "属性数字化编码", position = 6)
    private String dataCode;
    @ApiModelProperty(value = "属性名称", position = 7)
    private String propName;
    @ApiModelProperty(value = "属性编码", position = 8)
    private String propCode;
    @ApiModelProperty(value = "属性模型Code（属性模型短码）", position = 9)
    private String propModelCode;
    @ApiModelProperty(value = "属性取值", position = 10)
    private String propVal;

    @ApiModelProperty(value = "公式", position = 11)
    private String formula;

    @ApiModelProperty(value = "单位", position = 12)
    private String unit;
}
