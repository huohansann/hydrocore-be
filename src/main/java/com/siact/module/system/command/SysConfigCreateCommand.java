package com.siact.module.system.command;

import com.siact.module.system.enums.SysConfigModuleEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 系统配置创建命令
 *
 * @author siact
 */
@Data
public class SysConfigCreateCommand {

    @ApiModelProperty("模块")
    @NotNull(message = "[模块]不能为空")
    private SysConfigModuleEnum module;

    @ApiModelProperty("配置编码")
    @NotBlank(message = "[配置编码]不能为空")
    private String scCode;

    @ApiModelProperty("配置名称")
    @NotBlank(message = "[配置名称]不能为空")
    private String scName;

    @ApiModelProperty("配置说明")
    @NotBlank(message = "[配置说明]不能为空")
    private String description;

    @ApiModelProperty("配置数据（JSON 对象或数组）")
    @NotNull(message = "[配置数据]不能为空")
    private Object data;
}