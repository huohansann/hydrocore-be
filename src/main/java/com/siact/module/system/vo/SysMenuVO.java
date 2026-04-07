package com.siact.module.system.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("菜单信息")
public class SysMenuVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "菜单ID")
    private Long id;

    @ApiModelProperty(value = "父级菜单ID")
    private Long parentId;

    @ApiModelProperty(value = "菜单名称")
    private String menuName;

    @ApiModelProperty(value = "菜单编码")
    private String menuCode;

    @ApiModelProperty(value = "路由地址")
    private String path;

    @ApiModelProperty(value = "菜单图标")
    private String icon;

    @ApiModelProperty(value = "排序序号")
    private Integer sort;

    @ApiModelProperty(value = "菜单类型：1=目录，2=菜单")
    private Integer type;

    @ApiModelProperty(value = "是否显示")
    private Boolean visible;

    @ApiModelProperty(value = "状态")
    private Boolean status;
}
