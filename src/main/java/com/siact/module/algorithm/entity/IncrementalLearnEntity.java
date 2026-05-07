package com.siact.module.algorithm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("incremental_learn")
public class IncrementalLearnEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String dataCode;

    private String targetName;

    private String modelPath;

    private BigDecimal valLoss;

    private BigDecimal valMae;

    private BigDecimal valRmse;

    private BigDecimal valR2;

    private BigDecimal testLoss;

    private BigDecimal testMae;

    private BigDecimal testRmse;

    private BigDecimal testR2;

    private Boolean validity;

    private String remark;
}