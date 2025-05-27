package com.siact.module.forecast.vo;

import lombok.Data;

import java.util.List;

/**
 * @desc:
 * @author: zhangwentao
 * @create: 2025-05-27 17:27
 */
@Data
public class AttributeInfoVO {
    private String name;
    private List<Object[]> value;
}