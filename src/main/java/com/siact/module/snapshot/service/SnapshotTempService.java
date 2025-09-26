package com.siact.module.snapshot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.snapshot.entity.SnapshotTempEntity;

import java.util.List;

/**
 * 快照服务接口
 *
 * @author Roo
 * @date 2025-09-22
 */
public interface SnapshotTempService extends IService<SnapshotTempEntity> {

    /**
     * 根据数据编码列表查询温度数据
     * @param dataCodeList 数据编码列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 温度数据列表
     */
    List<SnapshotTempEntity> queryByDataCodeInRange(List<String> dataCodeList, String startTime, String endTime);
}