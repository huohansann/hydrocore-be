package com.siact.module.permission.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.siact.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 组织实体类
 *
 * @author example
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_organization")
public class OrganizationEntity extends BaseEntity {
    
    /**
     * 组织名称
     */
    private String name;
    
    /**
     * 组织编码
     */
    private String code;
    
    /**
     * 父级ID
     */
    private Long parentId;
    
    /**
     * 祖级列表
     */
    private String ancestors;
    
    /**
     * 显示顺序
     */
    private Integer sort;
    
    /**
     * 负责人
     */
    private String leader;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 状态（1正常 0停用）
     */
    private Boolean status;
    
    /**
     * 子组织列表
     */
    @TableField(exist = false)
    private List<OrganizationEntity> children = new ArrayList<>();
} 