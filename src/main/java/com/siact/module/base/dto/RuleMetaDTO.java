package com.siact.module.base.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class RuleMetaDTO implements Serializable {
    private Long id;
    private String ruleCode;
    private String ruleName;
    private Integer status;
    private String createTime;
    private String updateTime;
} 