package com.siact.module.forecast.query;

import com.siact.common.validated.StringContains;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class TempActualForecastQuery {

    @NotEmpty(message = "属性数字化编码不能为空")
    private List<String> dataCodes;

    @NotBlank(message = "开始时间不能为空")
    private String startTime;

    @NotBlank(message = "结束时间不能为空")
    private String endTime;

    @NotNull(message = "步长不能为空")
    private Integer ts;

    @StringContains(limitValues = {"Y", "M", "D", "H", "MIN"}, message = "步长单位不正确")
    private String tsUnit;

    private String formatVal;

    @StringContains(limitValues = {"AVG", "MAX", "MIN", "LAST", "FIRST", "TOTAL", "INC", "SUM", "COUNT"}, message = "计算类型不正确")
    private String calcType;

    private List<String> names;
}
