package com.siact.module.snapshot.service;

import com.siact.module.snapshot.dto.SnapshotChartQueryDTO;
import com.siact.module.snapshot.vo.SnapshotChartVO;

/**
 * 快照服务接口
 *
 * @author Roo
 * @date 2025-09-22
 */
public interface SnapshotPublicService {

    SnapshotChartVO queryChart(SnapshotChartQueryDTO queryDTO);

    /**
     * 执行快照任务
     */
    void execSnapshotTask(String nowTime);

}