package com.siact.module.forecast.dto;

import com.siact.common.validated.StringContains;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @desc:
 * @author: zhangwentao
 * @create: 2025-05-26 10:35
 */
@ApiModel(description = "窑炉预测数据查询参数")
@Data
public class ForecastKilnParamsDTO {
    @ApiModelProperty(value = "模板编码")
    List<String> dataCodes;

    @ApiModelProperty(value = "开始时间")
    private String startTime;

    @ApiModelProperty(value = "结束时间")
    private String endTime;

    @NotNull(message = "步长不能为空")
    @ApiModelProperty(value = "步长")
    private Integer ts;

    @StringContains(limitValues = {"Y" ,"M", "D", "H", "MIN"}, message = "步长单位不正确")
    @ApiModelProperty(value = "步长单位(Y:年;M:月;D:日;H:小时;MIN:分)")
    private String tsUnit;

    @ApiModelProperty(value = "返回时间格式")
    private String formatVal;

    @StringContains(limitValues = {"AVG", "MAX", "MIN", "LAST", "FIRST", "TOTAL", "INC", "SUM", "COUNT"},message = "计算类型不正确")
    @ApiModelProperty(value = "计算类型(AVG:均值;MAX:最大值;MIN:最小值;LAST:最新值;FIRST:最早值;SUM:累加;INC:增量;COUNT:数量)")
    private String calcType;

    @ApiModelProperty(value = "名称")
    List<String> names;
}