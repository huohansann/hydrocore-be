package com.siact.module.base.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControlIntervalConfigVO {
    /**
     * 主键
     */
    @ApiModelProperty(value = "主键")
    private Long id;


    /**
     * 测点，温度中有MC1-10
     */
    @ApiModelProperty(value = "测点：MC1-10")
    private String measurePoint;

    /**
     * 测点类型，暂时预留一下
     */
    @ApiModelProperty(value = "测点类型：temperature、liquidLevel、pressure")
    private String pointType;

    private String startTime;

    private String endTime;

    private Integer ts;

    private String tsUnit;

    private String formatVal;
}
