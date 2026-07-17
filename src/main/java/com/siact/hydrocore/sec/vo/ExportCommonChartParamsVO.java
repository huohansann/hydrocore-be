package com.siact.hydrocore.sec.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ExportCommonChartParamsVO extends CommonChartParamsVo {
    public ExportCommonChartParamsVO() {
        super();
    }

    @ApiModelProperty(value = "属性名")
    @NotBlank(message = "属性名不能为空")
    private String propNames;

    @ApiModelProperty(value = "文件名")
    @NotBlank(message = "文件名不能为空")
    private String fileName;
}
