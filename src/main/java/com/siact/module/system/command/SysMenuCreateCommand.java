package com.siact.module.system.command;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("新增菜单")
public class SysMenuCreateCommand {

    @ApiModelProperty(value = "父级菜单ID，顶级为0")
    private Long parentId = 0L;

    @NotBlank(message = "菜单名称不能为空")
    @ApiModelProperty(value = "菜单名称")
    private String menuName;

    @ApiModelProperty(value = "菜单编码")
    private String menuCode;

    @NotBlank(message = "路由地址不能为空")
    @ApiModelProperty(value = "路由地址")
    private String path;

    @ApiModelProperty(value = "菜单图标")
    private String icon;

    @ApiModelProperty(value = "排序序号")
    private Integer sort = 0;

    @NotNull(message = "菜单类型不能为空")
    @ApiModelProperty(value = "菜单类型：1=目录，2=菜单")
    private Integer type;

    @ApiModelProperty(value = "是否显示")
    private Boolean visible = true;
}
