package com.siact.hydrocore.sec.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class CommonChartParamsVo extends BaseVo {
    public CommonChartParamsVo() {super();}

    @NotEmpty(message = "属性数字化编码不能为空")
    @ApiModelProperty(value = "数字孪生编码列表")
    private List<String> dataCodes;

    @ApiModelProperty(value = "属性数字化短码")
    private List<String> propModelCodes;
}
