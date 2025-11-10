package com.siact.sec.vo;

import com.siact.common.validated.StringContains;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 查询的五个必须 变量
 * @author dell
 */

@Data
public class FiveBaseVo {

    @NotBlank(message = "开始时间不能为空")
//    @Pattern(regexp = "(\\d{4}-\\d{2}-\\d{2})\\s([0-2][0-3]:[0-5][0-9]:[0-5][0-9])", message = "日期格式不正确，应该为 yyyy-MM-dd hh:mm:ss 格式!")
    @ApiModelProperty(value = "开始时间,格式：yyyy-MM-dd hh:mm:ss")
    private String startTime;

    @NotBlank(message = "结束时间不能为空")
//    @Pattern(regexp = "(\\d{4}-\\d{2}-\\d{2})\\s([0-2][0-3]:[0-5][0-9]:[0-5][0-9])", message = "日期格式不正确，应该为 yyyy-MM-dd hh:mm:ss 格式!")
    @ApiModelProperty(value = "结束时间,格式：yyyy-MM-dd hh:mm:ss")
    private String endTime;

    @NotNull(message = "步长不能为空")
    @ApiModelProperty(value = "步长")
    private Integer ts;

    @StringContains(limitValues = {"Y" ,"M", "D", "H", "MIN","S"}, message = "步长单位不正确")
    @ApiModelProperty(value = "步长单位(Y:年;M:月;D:日;H:小时;MIN:分,S:秒)")
    private String tsUnit;

    @StringContains(limitValues = {"AVG", "MAX", "MIN", "LAST", "FIRST", "TOTAL", "INC", "SUM", "COUNT"},message = "计算类型不正确")
    @ApiModelProperty(value = "计算类型(AVG:均值;MAX:最大值;MIN:最小值;LAST:最新值;FIRST:最早值;SUM:累加;INC:增量;COUNT:数量)")
    private String calcType;
}
