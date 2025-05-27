package com.siact.module.mqtt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@TableName("mqtt_record")
@Data
public class MqttRecordEntity {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    private String topic;
    private String message;
    private Date createTime;
}
