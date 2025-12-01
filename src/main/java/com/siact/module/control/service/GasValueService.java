package com.siact.module.control.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.control.entity.GasValueEntity;

import java.util.List;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-11-28 10:29
 * @className : GasValueService
 * @description : 天然气运行值业务层
 */
public interface GasValueService extends IService<GasValueEntity> {
    /**
     * 根据 {@code time} 查询对应的天然气运行值数据
     *
     * @param time {@link String} 要查询的天然气值运行时间
     * @return 返回 {@code time} 时刻的天然气运行值集合
     */
    List<GasValueEntity> queryByTime(String time);
}
