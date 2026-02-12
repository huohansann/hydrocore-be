package com.siact.module.algorithm.enums;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-08 14:19
 * @className : IntelliTypeEnum
 * @description : 智能计算结果类型枚举类
 */
public enum IntelliTypeEnum {
    // 天然气智控值
    GAS_TRANSFORMER_MODEL,
    // 天然气智控值 Delta_C
    GAS_DELTAC_MODEL,
    // 天然气智控值 Delta_C(基于专家经验)
    GAS_DELTAC_EXPERT,
    // 上一次天然气运行值
    GAS_LAST_SUM,

    // 预测最小温度
    MIN_TEMP,
    // 预测最大温度
    MAX_TEMP,
    // 天然气运行值
    GAS_RUN_VALUE,
    // 天然气智控值 method1 (基于 model)
    GAS_CALC_MODEL1,
    // 天然气智控值 method2 (基于 model)
    GAS_CALC_MODEL2,
    // 天然气智控值 method1 (基于专家经验)
    GAS_CALC_EXPERT1,
    // 天然气智控值 method2 (基于专家经验)
    GAS_CALC_EXPERT2
}
