package com.siact.module.predicted.mqtt.handler;

import com.siact.module.predicted.mqtt.linstener.PredictedMqttListener;
import com.siact.mqtt.config.CustomMqttClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class PredictedMqttHandler {

    @Value("${mqtt.topic.predictedRecord:PREDICTED_RECORD_TOPIC}")
    private String notificationTopic;

    @Autowired(required = false)
    private CustomMqttClient mqttClient;

    @Autowired
    private PredictedMqttListener predictedMqttListener;


    @PostConstruct
    public void handleNotification() {
        if (ObjectUtils.isEmpty(mqttClient)) {
            log.warn("mqttClient初始化失败!请确保mqtt的配置值正确");
            return;
        }
        mqttClient.subscribeWithResponse(notificationTopic, predictedMqttListener);
    }
}
