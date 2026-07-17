package com.siact.hydrocore.module.mqtt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.hydrocore.module.mqtt.entity.MqttRecordEntity;
import com.siact.hydrocore.module.mqtt.mapper.MqttRecordMapper;
import com.siact.hydrocore.module.mqtt.service.MqttRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MqttRecordServiceImpl extends ServiceImpl<MqttRecordMapper, MqttRecordEntity> implements MqttRecordService {
}
