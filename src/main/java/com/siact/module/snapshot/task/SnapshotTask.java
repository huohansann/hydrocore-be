package com.siact.module.snapshot.task;

import com.siact.common.constant.ConstantTime;
import com.siact.common.utils.TimeUtil;
import com.siact.module.snapshot.service.SnapshotPublicService;
import com.siact.sec.utils.IntervalTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SnapshotTask {

    @Autowired
    private SnapshotPublicService snapshotService;

    /**
     * 快照任务
     * 定时任务，每1分钟执行一次
     * 由于要考虑算法和孪生的实时数据, 所以当前定时任务延迟30秒执行
     */
    @Scheduled(cron = "30 0/1 * * * ?")
    public void snapshotTask() {
        log.info("快照任务开始执行");
        // 当前时间(将秒归0,   防止查询范围有误)
        String nowTime = IntervalTimeUtil.dateFormat(TimeUtil.getNow(), ConstantTime.DATE_TIME_MM_00);

        snapshotService.execSnapshotTask(nowTime);

        log.info("快照任务执行结束");
    }
}
