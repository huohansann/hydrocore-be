package com.siact.module.control.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.module.control.entity.ExpertExperienceEntity;
import com.siact.module.control.mapper.ExpertExperienceMapper;
import com.siact.module.control.service.ExpertExperienceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-11-28 11:00
 * @className : ExpertExperienceServiceImpl
 * @description : 智能计算值(基于专家经验)业务层实现
 */
@Service
public class ExpertExperienceServiceImpl extends ServiceImpl<ExpertExperienceMapper, ExpertExperienceEntity> implements ExpertExperienceService {
    private @Resource ExpertExperienceMapper mapper;

    /**
     * 根据结果响应时间返回最后一条智能计算(基于专家经验)值
     *
     * @return 返回最后一条智能计算值(基于专家经验)
     */
    @Override
    public ExpertExperienceEntity queryWithResultTime() {
        LambdaQueryWrapper<ExpertExperienceEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ExpertExperienceEntity::getResultTime);
        wrapper.last("limit 1");
        return mapper.selectOne(wrapper);
    }
}
