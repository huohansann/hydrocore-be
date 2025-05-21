package com.siact.module.permission.vo;

import com.siact.common.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 菜单实体类
 *
 * @author wr
 */
@ApiModel(description = "菜单树状结构")
@Data
public class MenuVO extends BaseEntity {
    @ApiModelProperty(value = "父级ID")
    private Long parentId;

    @ApiModelProperty(value = "菜单名称")
    private String menuName;

    @ApiModelProperty(value = "菜单code")
    private String menuCode;

    @ApiModelProperty(value = "路由地址")
    private String menuUrl;

    @ApiModelProperty(value = "类型（0目录 1菜单 2按钮）")
    private Integer type;

    @ApiModelProperty(value = "菜单图标")
    private String menuIcon;

    @ApiModelProperty(value = "是否显示（1是 0否）")
    private Boolean modelShow;

    @ApiModelProperty(value = "状态（1正常 0停用）")
    private Boolean status;

    @ApiModelProperty(value = "子菜单列表")
    private List<MenuVO> children ;
}