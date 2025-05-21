package com.siact.module.permission.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 菜单请求DTO
 *
 * @author example
 */
@Data
@ApiModel(value = "菜单请求参数")
public class MenuDTO {
    
    @ApiModelProperty(value = "菜单ID", example = "1")
    private Long id;
    
    @ApiModelProperty(value = "父级ID", example = "0")
    private Long parentId;
    
    @ApiModelProperty(value = "名称", required = true, example = "项目1")
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 50, message = "项目名称长度不能超过50个字符")
    private String menuName;

    @ApiModelProperty(value = "编码", required = true, example = "system")
    private String menuCode;

    @ApiModelProperty(value = "路由地址", example = "/system")
    @Size(max = 200, message = "路由地址长度不能超过200个字符")
    private String menuUrl;
    
    @ApiModelProperty(value = "类型（0菜单 1实例 2项目）", example = "2")
    private Integer type;
    
    @ApiModelProperty(value = "图标", example = "system")
    @Size(max = 100, message = "图标长度不能超过100个字符")
    private String menuIcon;
    
    @ApiModelProperty(value = "是否显示（1是 0否）", example = "true")
    private Boolean modelShow;
    
    @ApiModelProperty(value = "状态（1正常 0停用）", example = "true")
    private Boolean status;
} 