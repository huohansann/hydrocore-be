package com.siact.common.query;

import lombok.Getter;
import lombok.Setter;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-19 10:01
 * @className : PageQuery
 * @description : 分页查询请求基础对象
 */
@Getter
@Setter
public class PageQuery {
    private Integer page = 1;
    private Integer pageSize = 10;
}
