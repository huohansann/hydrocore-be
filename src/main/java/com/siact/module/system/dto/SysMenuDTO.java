package com.siact.module.system.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-19 10:44
 * @className : SysMenuDTO
 * @description : 系统菜单传输对象
 */
@Getter
@Setter
public class SysMenuDTO {
    private Long id;
    private String code;
    private String label;
    private Integer sort;
    private Boolean show;
    private Boolean disabled;
    private String target;
}
