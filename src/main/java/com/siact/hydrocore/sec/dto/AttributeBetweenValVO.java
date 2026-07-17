package com.siact.hydrocore.sec.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @desc:
 * @author: zhangwentao
 * @create: 2025-04-21 16:13
 */
@Data()
@ApiModel(description = "属性两时间点之间的值")
public class AttributeBetweenValVO {

    @NotEmpty(message = "属性code不能为空！")
    @ApiModelProperty(value ="属性code")
    private List<String> dataCodes;

    @ApiModelProperty(value = "开始时间")
    private String startTime;

    @ApiModelProperty(value ="结束时间")
    private String endTime;

    @ApiModelProperty(value ="计算类型",notes = "AVG:均值;MAX:最大值;MIN:最小值;LAST:最新值;FIRST:最早值;SUM:累加;INC:增量;COUNT:数量")
    private String calcType;
}