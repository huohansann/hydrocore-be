package com.siact.module.algorithm.services;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.algorithm.entity.IntelligentDataEntity;

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
     * 调用自学习算法：查询历史数据 → 生成 JSON 文件 → 调用远程 Python 算法
     */
    void callSelfLearningAlgorithm();

    void callIncrementalLearn();
}
