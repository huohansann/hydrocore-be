package com.siact.module.permission.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 用户请求DTO
 *
 * @author example
 */
@Data
@ApiModel(value = "用户请求参数")
public class UserDTO {
    
    @ApiModelProperty(value = "用户ID", example = "1")
    private Long id;
    
    @ApiModelProperty(value = "用户名", required = true, example = "admin")
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[\\u4E00-\\u9FA5a-zA-Z0-9_]+$", message = "用户名称只能包含中文、字母、数字、下划线")
    private String username;

    @ApiModelProperty(value = "手机号码", example = "13800138000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    private String mobile;

    @ApiModelProperty(value = "密码", example = "123456")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    @ApiModelProperty(value = "性别（0未知 1男 2女）", example = "1")
    private Integer gender;

    @ApiModelProperty(value = "用户账号", example = "zhangsan")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户账号只能包含字母、数字、下划线")
    private String account;

    @ApiModelProperty(value = "角色ID列表", example = "[1, 2, 3]")
    private List<Long> roleIds;

    @ApiModelProperty(value = "关联组织id", example = "[1, 2, 3]")
    private List<Long> orgIds;

    @ApiModelProperty(value = "状态（true:正常 false:停用）", example = "true")
    private Boolean status;

}