package com.siact.module.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("参数配置")
public class ModelConfigParamDTO {
    @ApiModelProperty(value = "时间范围")
    private List<ModelConfigParamDetailDTO> range;
    @ApiModelProperty(value = "重要/次要/一般参数")
    private List<ModelConfigParamDetailDTO> param;
}
