package com.siact.module.control.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.module.control.entity.IntelligentComputingEntity;
import com.siact.module.control.mapper.IntelligentComputingMapper;
import com.siact.module.control.service.IntelligentComputingService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-11-28 10:51
 * @className : IntelligentComputingServiceImpl
 * @description : 智能计算值业务层实现
 */
@Service
public class IntelligentComputingServiceImpl extends ServiceImpl<IntelligentComputingMapper, IntelligentComputingEntity> implements IntelligentComputingService {
    private @Resource IntelligentComputingMapper mapper;

    /**
     * 根据结果响应时间返回最后一条智能计算值
     *
     * @return 返回最后一条智能计算值
     */
    @Override
    public IntelligentComputingEntity queryWithResultTime() {
        LambdaQueryWrapper<IntelligentComputingEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(IntelligentComputingEntity::getResultTime);
        wrapper.last("limit 1");
        return mapper.selectOne(wrapper);
    }
}
