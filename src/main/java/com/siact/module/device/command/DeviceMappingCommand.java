package com.siact.module.device.command;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(description = "设备点位新增/修改")
public class DeviceMappingCommand {

    @ApiModelProperty("主键(修改时必传)")
    private Long id;

    @NotBlank(message = "现场点位名称不能为空")
    @ApiModelProperty("现场点位名称")
    private String pointName;

    @NotBlank(message = "点位ID不能为空")
    @ApiModelProperty("点位ID(TAOS_DB编码)")
    private String itemId;

    @NotBlank(message = "属性编码不能为空")
    @ApiModelProperty("属性编码(孪生长码)")
    private String propCode;

    @NotBlank(message = "属性名称不能为空")
    @ApiModelProperty("属性名称")
    private String propName;

    @NotBlank(message = "设备编码不能为空")
    @ApiModelProperty("设备编码")
    private String deviceCode;

    @NotBlank(message = "设备名称不能为空")
    @ApiModelProperty("设备名称")
    private String deviceName;

    @ApiModelProperty("备注")
    private String remark;
}
