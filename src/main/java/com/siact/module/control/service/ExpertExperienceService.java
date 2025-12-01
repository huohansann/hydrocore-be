package com.siact.module.control.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.control.entity.ExpertExperienceEntity;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-11-28 10:51
 * @className : ExpertExperienceService
 * @description : 智能计算值(基于专家经验)业务层
 */
public interface ExpertExperienceService extends IService<ExpertExperienceEntity> {
    /**
     * 根据结果响应时间返回最后一条智能计算(基于专家经验)值
     *
     * @return 返回最后一条智能计算值(基于专家经验)
     */
    ExpertExperienceEntity queryWithResultTime();
}
