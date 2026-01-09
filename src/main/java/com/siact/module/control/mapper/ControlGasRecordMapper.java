package com.siact.module.control.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.control.entity.ControlGasRecordEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-05 11:06
 * @className : ControlGasRecordMapper
 * @description : 天然气 dcs 运行值记录数据持久层
 */
@Mapper
public interface ControlGasRecordMapper extends BaseMapper<ControlGasRecordEntity> {
}
