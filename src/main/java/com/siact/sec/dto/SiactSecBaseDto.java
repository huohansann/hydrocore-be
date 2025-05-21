package com.siact.sec.dto;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class SiactSecBaseDto {

    @ApiModelProperty(value = "开始时间,格式：yyyy-MM-dd hh:mm:ss")
    private String startTime;


    @ApiModelProperty(value = "结束时间,格式：yyyy-MM-dd hh:mm:ss")
    private String endTime;

    @ApiModelProperty(value = "步长")
    private Integer ts;


    @ApiModelProperty(value = "步长单位(Y:年;M:月;D:日;H:小时;MIN:分)")
    private String tsUnit;

    @NotBlank(message = "格式化格式不能为空")
    @ApiModelProperty(value = "格式化格式，如MM-dd")
    private String formatVal;

    @ApiModelProperty(value = "计算类型(AVG:均值;MAX:最大值;MIN:最小值;LAST:最新值;FIRST:最早值;SUM:累加;INC:增量;COUNT:数量)")
    private String calcType;

    @ApiModelProperty(value = "名称集合")
    private List<String> names;

    @ApiModelProperty(value = "单位集合")
    private List<String> units;

    @ApiModelProperty(value = "是否展示表格集合")
    private List<Boolean> showTables;
}
