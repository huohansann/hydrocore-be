package com.siact.module.system.query;

import com.siact.common.query.PageQuery;
import lombok.Getter;
import lombok.Setter;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-19 9:29
 * @className : SysMenuQuery
 * @description : 菜单请求参数对向
 */
@Getter
@Setter
public class SysMenuQuery extends PageQuery {
    private Long parentId = 0L;
    private String code;
    private String name;
}
