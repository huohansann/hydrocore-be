package com.siact.module.control.repository;

import com.siact.common.repository.BaseRepository;
import com.siact.module.control.entity.ControlGasRecordEntity;

import java.util.Map;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-05 11:07
 * @className : ControlGasRecordRepository
 * @description : 天然气 dcs 运行值记录数据持久层
 */
public interface ControlGasRecordRepository extends BaseRepository<ControlGasRecordEntity> {
    Map<String, ControlGasRecordEntity> queryWithLastTime();
}
