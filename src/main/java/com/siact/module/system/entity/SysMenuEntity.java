package com.siact.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-17 9:25
 * @className : SysMenuEntity
 * @description : 系统菜单控制器
 */

@Data
@EqualsAndHashCode
@TableName("sys_menu_new")
public class SysMenuEntity {
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
    private Boolean isShow;
    /**
     * 是否禁用
     */
    private Boolean disabled;
    /**
     * 打开方式
     */
    private String target;

    /**
     * 创建者
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新者
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    private String remark;
}
