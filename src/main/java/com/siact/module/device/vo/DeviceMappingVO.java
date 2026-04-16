package com.siact.module.device.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.sql.Timestamp;

@Data
@ApiModel(description = "设备点位视图对象")
public class DeviceMappingVO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("现场点位名称")
    private String pointName;

    @ApiModelProperty("点位ID(TAOS_DB编码)")
    private String itemId;

    @ApiModelProperty("属性编码(孪生长码)")
    private String propCode;

    @ApiModelProperty("属性名称")
    private String propName;

    @ApiModelProperty("设备编码")
    private String deviceCode;

    @ApiModelProperty("设备名称")
    private String deviceName;

    @ApiModelProperty("创建时间")
    private Timestamp createTime;

    @ApiModelProperty("更新时间")
    private Timestamp updateTime;

    @ApiModelProperty("备注")
    private String remark;
}
