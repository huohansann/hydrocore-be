package com.siact.module.predicted.mqtt.handler;

import com.siact.module.predicted.mqtt.linstener.PredictedMqttListener;
import com.siact.mqtt.config.CustomMqttClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class PredictedMqttHandler {

    @Value("${mqtt.topic.predictedRecord:PREDICTED_RECORD_TOPIC}")
    private String notificationTopic;

    @Autowired
    private CustomMqttClient mqttClient;

    @Autowired
    private PredictedMqttListener predictedMqttListener;


    @PostConstruct
    public void handleNotification() {
        mqttClient.subscribeWithResponse(notificationTopic, predictedMqttListener);
    }
}
