package com.siact.module.permission.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 角色请求DTO
 *
 * @author example
 */
@Data
@ApiModel(value = "角色请求参数")
public class RoleDTO {
    
    @ApiModelProperty(value = "角色ID", example = "1")
    private Long id;
    
    @ApiModelProperty(value = "角色名称", required = true, example = "管理员")
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    private String name;
    
    @ApiModelProperty(value = "角色编码", example = "ADMIN")
    @Size(max = 50, message = "角色编码长度不能超过50个字符")
    private String code;
    
    @ApiModelProperty(value = "角色描述", example = "系统管理员，拥有所有权限")
    @Size(max = 500, message = "角色描述长度不能超过500个字符")
    private String description;
    
    @ApiModelProperty(value = "显示顺序", example = "1")
    private Integer sort;
    
    @ApiModelProperty(value = "状态（1正常 0停用）", example = "true")
    private Boolean status;
    
    @ApiModelProperty(value = "菜单ID列表", example = "[1, 2, 3]")
    private List<Long> menuIds;
} 