package com.siact.module.base.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@ApiModel("费价模型根据峰平谷返回信息")
public class FeeModelComputeMarkRtnDTO extends FeeModelBaseRtnDTO{

    @ApiModelProperty("峰平谷类型:peakValley-,单一类型:electricity-3")
    private String computeMark;

    @ApiModelProperty("分时计费/单一计费")
    private String style;

    public FeeModelComputeMarkRtnDTO(BigDecimal val, BigDecimal fee, String computeMark,String style) {
        super(val, fee);
        this.computeMark = computeMark;
        this.style = style;
    }
}
