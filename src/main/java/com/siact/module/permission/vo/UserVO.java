package com.siact.module.permission.vo;

import com.siact.module.permission.entity.OrganizationEntity;
import com.siact.module.permission.entity.RoleEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 用户实体类
 *
 * @author example
 */
@Data
@ApiModel(description = "用户详情")
public class UserVO  {

    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "手机号码")
    private String mobile;

    @ApiModelProperty(value = "所属组织ID")
    private Long orgId;

    @ApiModelProperty(value = "性别（0未知 1男 2女）")
    private Integer gender;

    @ApiModelProperty(value = "账号")
    private String account;

    @ApiModelProperty(value = "头像地址")
    private String avatar;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "状态（1正常 0停用）")
    private Boolean status;

    @ApiModelProperty(value = "角色列表")
    private List<RoleEntity> roleList;

    @ApiModelProperty(value = "组织列表")
    private List<OrganizationEntity> orgList;
} 