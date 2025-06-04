package com.siact.module.process.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.process.entity.ProcessLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工艺日志Mapper
 */
@Mapper
public interface ProcessLogMapper extends BaseMapper<ProcessLogEntity> {
} 