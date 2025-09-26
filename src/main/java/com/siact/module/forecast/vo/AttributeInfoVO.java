package com.siact.module.forecast.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @desc:
 * @author: zhangwentao
 * @create: 2025-05-27 17:27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributeInfoVO {
    private String name;
    private List<Object[]> value;
}