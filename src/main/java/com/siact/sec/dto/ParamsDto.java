package com.siact.sec.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@ApiModel(description = "属性转换参数")
public class ParamsDto {

    @NotEmpty(message = "属性数字化编码不能为空")
    @ApiModelProperty("属性数字化编码")
    private List<String> dataCodes;

    @ApiModelProperty("开始时间")
    private String startTime;

    @ApiModelProperty("结束时间")
    private String endTime;

    @ApiModelProperty("时间步长")
    private Integer ts;

    @ApiModelProperty("步长单位")
    private String tsUnit;

    @ApiModelProperty("计算类型")
    private String calcType;

    @ApiModelProperty("格式化格式")
    private String formatVal;
}
