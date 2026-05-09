package com.siact.module.level.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
public class LevelControlConfigDTO {

    @NotBlank(message = "控制模式不能为空")
    @ApiModelProperty(value = "控制模式：ai/pid/manual", required = true)
    private String mode;

    @ApiModelProperty(value = "AI预测窗口")
    private BigDecimal aiPredictWindow;

    @ApiModelProperty(value = "AI预测时长")
    private BigDecimal aiPredictDuration;

    @ApiModelProperty(value = "PID比例带PB")
    private BigDecimal pidPb;

    @ApiModelProperty(value = "PID积分时间TI")
    private BigDecimal pidTi;

    @ApiModelProperty(value = "PID微分时间TD")
    private BigDecimal pidTd;

    @ApiModelProperty(value = "人工控制值")
    private BigDecimal manualControlValue;

    @ApiModelProperty(value = "安全限制")
    private BigDecimal safeLimit;

    @ApiModelProperty(value = "开度上限")
    private BigDecimal openingUpperLimit;
}
