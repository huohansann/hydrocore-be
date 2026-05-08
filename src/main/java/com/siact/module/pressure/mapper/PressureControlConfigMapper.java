package com.siact.module.pressure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.pressure.entity.PressureControlConfigEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 窑压控制参数配置 Mapper
 */
@Mapper
public interface PressureControlConfigMapper extends BaseMapper<PressureControlConfigEntity> {
}
