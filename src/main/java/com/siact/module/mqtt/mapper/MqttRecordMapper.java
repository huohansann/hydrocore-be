package com.siact.module.mqtt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.mqtt.entity.MqttRecordEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MqttRecordMapper extends BaseMapper<MqttRecordEntity> {
}
