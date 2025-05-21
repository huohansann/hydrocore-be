package com.siact.sec.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class CommonChartParamsDto extends SiactSecBaseDto {

    public CommonChartParamsDto() {
        super();
    }


    @NotEmpty(message = "属性数字化编码不能为空")
    @ApiModelProperty(value = "数字孪生编码列表")
    private List<String> dataCodes;

    @ApiModelProperty(value = "属性数字化短码")
    private List<String> propModelCodes;

}
