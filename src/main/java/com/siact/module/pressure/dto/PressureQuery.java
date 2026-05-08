package com.siact.module.pressure.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author: HouBo
 * @Date: 2026/5/8 14:00
 * @Description: 窑压历史数据查询参数
 */
@Data
public class PressureQuery {

    @NotBlank(message = "属性数字化编码不能为空")
    @ApiModelProperty("数字孪生编码")
    private String dataCode;

    @NotBlank(message = "开始时间不能为空")
    @ApiModelProperty("开始时间")
    private String startTime;

    @NotBlank(message = "结束时间不能为空")
    @ApiModelProperty("结束时间")
    private String endTime;

    @NotNull(message = "步长不能为空")
    @ApiModelProperty("步长")
    private Integer ts;

    @ApiModelProperty("步长单位(Y/M/D/H/MIN)")
    private String tsUnit;

    @ApiModelProperty("计算类型(AVG/MAX/MIN/LAST/FIRST)")
    private String calcType;
}
