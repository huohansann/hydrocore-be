package com.siact.module.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AlgorithmDataCodeDTO {

    @ApiModelProperty("算法使用的code")
    private String algorithmCode;

    @ApiModelProperty("点位名称")
    private String name;

    @ApiModelProperty("孪生使用的code")
    private String dataCode;
}
