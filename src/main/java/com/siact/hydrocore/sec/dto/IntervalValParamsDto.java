package com.siact.hydrocore.sec.dto;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

@Data
public class IntervalValParamsDto {

    @NotEmpty(message = "属性数字化编码不能为空")
    private List<String> dataCodes;

    @NotBlank(message = "开始时间不能为空")
    @Pattern(regexp = "(\\d{4}-\\d{2}-\\d{2})\\s([0-2][0-3]:[0-5][0-9]:[0-5][0-9])", message = "日期格式不正确，应该为 yyyy-MM-dd hh:mm:ss 格式!")
    private String startTime;

    @NotBlank(message = "结束时间不能为空")
    @Pattern(regexp = "(\\d{4}-\\d{2}-\\d{2})\\s([0-2][0-3]:[0-5][0-9]:[0-5][0-9])", message = "日期格式不正确，应该为 yyyy-MM-dd hh:mm:ss 格式!")
    private String endTime;

    @NotNull(message = "步长不能为空")
    private Integer ts;

    @NotBlank(message = "步长单位不能为空")
    private String tsUnit;

    @NotBlank(message = "计算类型不能为空")
    private String calcType;

    @ApiModelProperty("格式化格式，如MM-dd")
    @NotBlank(message = "格式化格式不能为空")
    private String formatVal;
}
