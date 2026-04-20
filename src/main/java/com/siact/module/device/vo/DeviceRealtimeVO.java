package com.siact.module.device.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(description = "设备实时数据视图对象")
public class DeviceRealtimeVO {

    @ApiModelProperty("点位ID")
    private String itemId;

    @ApiModelProperty("属性名称")
    private String propName;

    @ApiModelProperty("属性编码")
    private String propCode;

    @ApiModelProperty("设备名称")
    private String deviceName;

    @ApiModelProperty("时间戳")
    private String ts;

    @ApiModelProperty("数值")
    private BigDecimal itemValue;
}
