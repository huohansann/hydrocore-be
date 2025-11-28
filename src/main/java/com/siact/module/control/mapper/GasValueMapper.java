package com.siact.module.control.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.control.entity.GasValueEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-11-27 15:59
 * @className : GasValueMapper
 * @description : 天然气运行值数据持久层
 */
@Mapper
public interface GasValueMapper extends BaseMapper<GasValueEntity> {

    Long insertBatch(@Param("list") List<GasValueEntity> batch);
}
