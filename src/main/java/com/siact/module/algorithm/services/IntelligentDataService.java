package com.siact.module.algorithm.services;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.algorithm.entity.IntelligentDataEntity;
import com.siact.module.algorithm.enums.IntelliTypeEnum;

import java.util.Map;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-08 14:26
 * @className : IntelligentDataService
 * @description : 智能计算算法业务类定义
 */
public interface IntelligentDataService extends IService<IntelligentDataEntity> {
    /**
     * 调用智能计算算法接口
     */
    void callIntelligentInterface();

    /**
     * 获取最后的天然气智控计算值
     */
    Map<String, IntelligentDataEntity> lastGasCalc();

    /**
     * 获取指定类型最后的时间点的智能算法值
     *
     * @param types 要查询的智能算法值类型
     * @return 返回 key 为 dataCode, 值为以类型分组的数据的查询结果
     */
    Map<String, Map<IntelliTypeEnum, IntelligentDataEntity>> queryByTypeWithLastTime(IntelliTypeEnum... types);
}
