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
}
