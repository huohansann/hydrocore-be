package com.siact.module.control.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.module.control.entity.GasValueEntity;
import com.siact.module.control.mapper.GasValueMapper;
import com.siact.module.control.service.GasValueService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-11-28 10:30
 * @className : GasValueServiceImpl
 * @description : 天然气运行值业务层实现
 */
@Service
public class GasValueServiceImpl extends ServiceImpl<GasValueMapper, GasValueEntity> implements GasValueService {
    private @Resource GasValueMapper mapper;

    /**
     * 根据 {@code time} 查询对应的天然气运行值数据
     *
     * @param time {@link String} 要查询的天然气值运行时间
     * @return 返回 {@code time} 时刻的天然气运行值集合
     */
    @Override
    public List<GasValueEntity> queryByTime(String time) {
        LambdaQueryWrapper<GasValueEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GasValueEntity::getTime, time);
        return mapper.selectList(wrapper);
    }
}
