package com.siact.hydrocore.module.system.command;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 系统配置更新命令
 *
 * @author siact
 */
@Data
public class SysConfigUpdateCommand {

    @ApiModelProperty("配置名称")
    private String scName;

    @ApiModelProperty("配置说明")
    private String description;

    @ApiModelProperty("配置数据（JSON 对象或数组）")
    @NotNull(message = "[配置数据]不能为空")
    private Object data;

    @ApiModelProperty("乐观锁版本号")
    @NotNull(message = "[版本号]不能为空")
    private Integer version;
}