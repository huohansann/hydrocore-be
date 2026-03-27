package com.siact.module.control.dto;

import com.siact.common.validated.StringContains;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author : kzuo
 * @version : 1.0
 * @date : 2026-03-26 10:18
 * @className : GasForecastQueryDTO
 * @description : 天然气曲线查询参数
 */
@Data
public class GasForecastQueryDTO {
    @NotEmpty(message = "属性数字化编码不能为空")
    private List<String> dataCodes;

    private List<String> names;

    @NotBlank(message = "开始时间不能为空")
    private String startTime;

    @NotBlank(message = "结束时间不能为空")
    private String endTime;

    @NotNull(message = "步长不能为空")
    private Integer ts;

    @StringContains(limitValues = {"Y", "M", "D", "H", "MIN"}, message = "步长单位不正确")
    private String tsUnit;

    @ApiModelProperty(value = "返回时间格式")
    private String formatVal;

    @StringContains(limitValues = {"AVG", "MAX", "MIN", "LAST", "FIRST", "TOTAL", "INC", "SUM", "COUNT"}, message = "计算类型不正确")
    private String calcType;
}
