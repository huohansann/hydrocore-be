package com.siact.module.control.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("intelligent_computing")
@Data
public class IntelligentComputingEntity {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 结果时间
     */
    private String resultTime;

    /**
     * mc1
     */
    private Double mc1;

    /**
     * mc2
     */
    private Double mc2;

    /**
     * mc3
     */
    private Double mc3;

    /**
     * mc4
     */
    private Double mc4;

    /**
     * mc5
     */
    private Double mc5;

    /**
     * mc6
     */
    private Double mc6;

    /**
     * mc7
     */
    private Double mc7;

    /**
     * mc8
     */
    private Double mc8;

    /**
     * mc9
     */
    private Double mc9;

    /**
     * mc10
     */
    private Double mc10;

    /**
     * 算法返回值
     */
    private String data;
}
