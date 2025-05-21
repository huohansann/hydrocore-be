package com.siact.module.permission.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 组织请求DTO
 *
 * @author example
 */
@Data
@ApiModel(value = "组织请求参数")
public class OrganizationDTO {
    
    @ApiModelProperty(value = "组织ID", example = "1")
    private Long id;
    
    @ApiModelProperty(value = "组织名称", required = true, example = "技术部")
    @NotBlank(message = "组织名称不能为空")
    @Size(max = 100, message = "组织名称长度不能超过100个字符")
    private String name;
    
    @ApiModelProperty(value = "组织编码", example = "TECH")
    @Size(max = 50, message = "组织编码长度不能超过50个字符")
    private String code;
    
    @ApiModelProperty(value = "父级ID", example = "0")
    private Long parentId;
    
    @ApiModelProperty(value = "显示顺序", example = "1")
    private Integer sort;
    
    @ApiModelProperty(value = "负责人", example = "张三")
    @Size(max = 50, message = "负责人长度不能超过50个字符")
    private String leader;
    
    @ApiModelProperty(value = "联系电话", example = "13800138000")
    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    private String phone;
    
    @ApiModelProperty(value = "邮箱", example = "example@example.com")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;
    
    @ApiModelProperty(value = "状态（1正常 0停用）", example = "true")
    private Boolean status;
} 