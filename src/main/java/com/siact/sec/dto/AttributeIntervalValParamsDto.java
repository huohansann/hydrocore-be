package com.siact.sec.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @desc: 请勿修改属性名称
 * @author: zhangwentao
 * @date: 2025-04-19 11:26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "属性区间值查询参数")
public class AttributeIntervalValParamsDto {

    @NotEmpty(message = "属性数字化编码不能为空")
    @ApiModelProperty(value = "属性数字化编码", required = true)
    private List<String> dataCodes;

    @ApiModelProperty(value = "开始时间", required = false)
    private String startTime;

    @ApiModelProperty(value = "结束时间", required = false)
    private String endTime;

    @ApiModelProperty(value = "时间步长", required = false)
    private Integer ts;

    @ApiModelProperty(value = "时间单位", required = false)
    private String tsUnit;

    @ApiModelProperty(value = "数据类型", required = false)
    private String calcType;
}
