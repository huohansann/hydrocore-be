package com.siact.module.base.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("费价模型基础返回信息")
public class FeeModelBaseRtnDTO {
    @ApiModelProperty("用量")
    private BigDecimal val;
    @ApiModelProperty("根据费价模型算得量费")
    private BigDecimal fee;
}
