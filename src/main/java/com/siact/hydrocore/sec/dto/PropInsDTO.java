package com.siact.hydrocore.sec.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PropInsDTO {
    @ApiModelProperty(value = "实例长码")
    private String insCode;

    @ApiModelProperty(value = "属性名")
    private String propName;

    @ApiModelProperty(value = "属性短码")
    private String shortCode;

    @ApiModelProperty(value = "属性长码")
    private String propCode;

    @ApiModelProperty(value = "属性值")
    private String propVal;

    @ApiModelProperty(value = "属性短码")
    private String unit;

}
