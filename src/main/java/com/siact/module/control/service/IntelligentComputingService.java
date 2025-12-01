package com.siact.module.control.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.control.entity.IntelligentComputingEntity;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-11-28 10:51
 * @className : IntelligentComputingService
 * @description : 智能计算值业务层
 */
public interface IntelligentComputingService extends IService<IntelligentComputingEntity> {
    /**
     * 根据结果响应时间返回最后一条智能计算值
     *
     * @return 返回最后一条智能计算值
     */
    IntelligentComputingEntity queryWithResultTime();
}
