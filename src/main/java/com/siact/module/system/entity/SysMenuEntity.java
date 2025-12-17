package com.siact.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.siact.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-17 9:25
 * @className : SysMenuEntity
 * @description : 系统菜单控制器
 */

@Data
@EqualsAndHashCode(callSuper = true)
// @TableName("sys_menu")
public class SysMenuEntity extends BaseEntity {
    /**
     * 菜单 ID
     */
    private @TableId(value = "id", type = IdType.ASSIGN_ID) Long id;
    /**
     * 父级菜单 ID
     */
    private Long parentId;
    /**
     * 菜单编码
     */
    private String code;
    /**
     * 菜单名称
     */
    private String label;
    /**
     * 菜单排序
     */
    private Integer sort;
    /**
     * 是否显示
     */
    private Boolean show;
    /**
     * 是否禁用
     */
    private Boolean disabled;
    /**
     * 打开方式
     */
    private String target;

    private String remark;
}
