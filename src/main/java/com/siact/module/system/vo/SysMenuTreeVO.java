package com.siact.module.system.vo;

import lombok.Data;

import java.util.List;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-19 14:08
 * @className : SysMenuTreeVO
 * @description : 系统菜单树视图对象
 */
@Data
public class SysMenuTreeVO {
    private Long id;
    private String code;
    private String name;
    private Long parentId;
    private Boolean show;
    private Boolean disabled;
    private Integer sort;
    private List<SysMenuTreeVO> children;
}
