package com.siact.module.base.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.siact.module.base.dto.TempConditionDTO;
import com.siact.module.base.dto.GasOperationDTO;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class RuleDetailVO implements Serializable {
    private Long id;
    private String ruleCode;
    private String ruleName;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    private List<TempConditionDTO> tempConditions;
    private List<GasOperationDTO> gasOperations;
} 