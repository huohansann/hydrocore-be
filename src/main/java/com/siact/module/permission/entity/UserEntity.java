package com.siact.module.permission.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.siact.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户实体类
 *
 * @author example
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class UserEntity extends BaseEntity {
    
    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号码
     */
    private String mobile;
    
    /**
     * 密码
     */
    private String password;

    /**
     * 所属组织ID
     */
    private Long orgId;

    
    /**
     * 性别（0未知 1男 2女）
     */
    private Integer gender;

    /**
     * 账号
     */
    private String account;

    /**
     * 头像地址
     */
    private String avatar;
    
    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态（1正常 0停用）
     */
    private Boolean status;
    
    /**
     * 角色ID列表
     */
    @TableField(exist = false)
    private List<Long> roleIds = new ArrayList<>();
    
    /**
     * 组织ID列表
     */
    @TableField(exist = false)
    private List<Long> orgIds = new ArrayList<>();
} 