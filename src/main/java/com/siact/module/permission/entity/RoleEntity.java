package com.siact.module.permission.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.siact.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色实体类
 *
 * @author example
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class RoleEntity extends BaseEntity {
    
    /**
     * 角色名称
     */
    private String name;
    
    /**
     * 角色编码
     */
    private String code;
    
    /**
     * 角色描述
     */
    private String description;
    
    /**
     * 显示顺序
     */
    private Integer sort;
    
    /**
     * 状态（1正常 0停用）
     */
    private Boolean status;
    
    /**
     * 菜单ID列表
     */
    @TableField(exist = false)
    private List<Long> menuIds = new ArrayList<>();
} 