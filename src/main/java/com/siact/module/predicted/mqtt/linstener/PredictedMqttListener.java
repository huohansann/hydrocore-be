package com.siact.module.predicted.mqtt.linstener;


import com.siact.module.predicted.service.PredictedDataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 监听更新消息
 */
@Slf4j
@Component
public class PredictedMqttListener implements IMqttMessageListener {

    @Autowired
    private PredictedDataService predictedDataService;

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());
        log.info("Received message on topic: " + topic + " - Payload: " + payload);

        if (StringUtils.isBlank(payload)) {
            // TODO 是否记录日志
            log.error("消息体信息不全: {}", payload);
            return;
        }

        predictedDataService.handleMqttMessage(topic, payload);
    }
}
