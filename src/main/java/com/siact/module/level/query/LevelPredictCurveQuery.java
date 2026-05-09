package com.siact.module.level.query;

import com.siact.common.validated.StringContains;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class LevelPredictCurveQuery {

    @NotBlank(message = "开始时间不能为空")
    @ApiModelProperty(value = "开始时间 yyyy-MM-dd HH:mm:ss", required = true)
    private String startTime;

    @NotBlank(message = "结束时间不能为空")
    @ApiModelProperty(value = "结束时间 yyyy-MM-dd HH:mm:ss", required = true)
    private String endTime;

    @NotNull(message = "步长不能为空")
    @ApiModelProperty(value = "步长", required = true)
    private Integer ts;

    @StringContains(limitValues = {"Y", "M", "D", "H", "MIN"}, message = "步长单位不正确")
    @ApiModelProperty(value = "步长单位：Y/M/D/H/MIN", required = true)
    private String tsUnit;

    @StringContains(limitValues = {"AVG", "MAX", "MIN", "LAST", "FIRST", "SUM", "COUNT"}, message = "计算类型不正确")
    @ApiModelProperty(value = "计算类型：AVG/MAX/MIN/LAST/FIRST/SUM/COUNT")
    private String calcType;

    @ApiModelProperty(value = "时间格式化，如 HH:mm、yyyy-MM-dd HH:mm")
    private String formatVal;
}
