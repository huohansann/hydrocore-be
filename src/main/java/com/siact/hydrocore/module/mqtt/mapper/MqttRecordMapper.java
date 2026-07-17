package com.siact.hydrocore.module.mqtt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.hydrocore.module.mqtt.entity.MqttRecordEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MqttRecordMapper extends BaseMapper<MqttRecordEntity> {
}
