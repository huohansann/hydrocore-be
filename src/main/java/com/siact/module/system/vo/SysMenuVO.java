package com.siact.module.system.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-18 16:43
 * @className : SysMenuVO
 * @description : 菜单数据传输对象
 */
@Data
public class SysMenuVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String code;
    private String name;
    private Integer sort;
    private Boolean show;
    private Boolean disabled;
    private String target;
}
