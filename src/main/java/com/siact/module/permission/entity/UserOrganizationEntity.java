package com.siact.module.permission.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户组织关联实体类
 *
 * @author example
 */
@Data
@TableName("sys_user_organization")
public class UserOrganizationEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 组织ID
     */
    private Long orgId;
    
    /**
     * 创建时间
     */
    private Date createTime;
} 