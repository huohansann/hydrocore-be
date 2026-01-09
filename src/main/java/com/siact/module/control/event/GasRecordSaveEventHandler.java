package com.siact.module.control.event;

import com.siact.core.event.annotation.EventTransactional;
import com.siact.core.event.domain.GenericEvent;
import com.siact.core.event.exception.EventHandleException;
import com.siact.core.event.handler.EventHandler;
import com.siact.module.control.entity.ControlGasRecordEntity;
import com.siact.module.control.repository.ControlGasRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-05 15:43
 * @className : GasRecordSaveEventHandler
 * @description : 天然气记录保存事件处理器
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class GasRecordSaveEventHandler implements EventHandler<GenericEvent> {
    public static final String EVENT_TYPE = "GAS_RECORD_SAVE";
    private final ControlGasRecordRepository repository;

    /**
     * 判断是否支持处理此事件
     */
    @Override
    public boolean supports(String eventType) {
        return EVENT_TYPE.equals(eventType);
    }

    /**
     * 处理事件
     */
    @Override
    @EventTransactional
    public @SuppressWarnings("unchecked") void handle(GenericEvent event) throws EventHandleException {
        List<ControlGasRecordEntity> data = (List<ControlGasRecordEntity>) event.getData();
        if (CollectionUtils.isNotEmpty(data)) repository.saveBatch(data);
    }
}
