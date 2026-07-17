package com.siact.hydrocore.module.device.query;

import com.siact.hydrocore.common.query.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "设备点位查询条件")
public class DeviceMappingQuery extends PageQuery {

    @ApiModelProperty("现场点位名称(模糊)")
    private String pointName;

    @ApiModelProperty("点位ID(精确)")
    private String itemId;

    @ApiModelProperty("属性编码(精确)")
    private String propCode;

    @ApiModelProperty("属性名称(模糊)")
    private String propName;

    @ApiModelProperty("设备编码(精确)")
    private String deviceCode;

    @ApiModelProperty("设备名称(模糊)")
    private String deviceName;
}
