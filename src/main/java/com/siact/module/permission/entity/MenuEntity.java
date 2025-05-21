package com.siact.module.permission.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.siact.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单实体类
 *
 * @author example
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class MenuEntity extends BaseEntity {
    
    /**
     * 父级ID
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单code
     */
    private String menuCode;
    
    /**
     * 路由地址
     */
    private String menuUrl;

    
    /**
     * 类型（0菜单 1实例 2项目）
     */
    private Integer type;
    
    /**
     * 菜单图标
     */
    private String menuIcon;
    
    /**
     * 是否显示（1是 0否）
     */
    private Boolean modelShow;
    
    /**
     * 状态（1正常 0停用）
     */
    private Boolean status;

}