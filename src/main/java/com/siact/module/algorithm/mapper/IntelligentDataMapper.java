package com.siact.module.algorithm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.algorithm.entity.IntelligentDataEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-08 14:29
 * @className : IntelligentDataMapper
 * @description : 智能计算算法数据持久层
 */
@Mapper
public interface IntelligentDataMapper extends BaseMapper<IntelligentDataEntity> {
}
