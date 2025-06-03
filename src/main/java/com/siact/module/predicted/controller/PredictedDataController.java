package com.siact.module.predicted.controller;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.siact.common.R;
import com.siact.module.predicted.enums.PredictedTypeEnum;
import com.siact.module.predicted.service.PredictedDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Api(tags = "预测数据")
@RestController
@RequestMapping("/predicted")
public class PredictedDataController {

    @Autowired
    private PredictedDataService predictedDataService;

    @ApiOperation(value = "处理预测message")
    @GetMapping("/handleMessage")
    public R handleMessageData(String topic, String message) {

        List<String> dataCodeList = new ArrayList<>(Arrays.asList("PGY02014_SZL01001_STZL001001_UBH001001_EQ000000000000_MPYWF2001", "PGY02014_SZL01001_STZL001001_UBH001001_EQ000000000000_MPYSS2001", "PGY02014_SZL01001_STZL001001_UBH001001_EQ000000000000_MPYGZ2001"));

        DateTime dateTime = new DateTime("2025-05-29 00:00:00");

        int day = 1;
        for (int i = 0; i < day * 1440; i++) {

            createSingleData(topic, dataCodeList, dateTime);
//            createMultiData(topic, dataCodeList, dateTime);
            // 向后走1S
            dateTime = dateTime.offsetNew(DateField.MINUTE, 1);
        }

        return R.success();
    }

    private void createMultiData(String topic, List<String> dataCodeList, DateTime dateTime) {
        JSONArray messageArray = new JSONArray();
        for (String dataCode : dataCodeList) {
            // 每次会生80个数据
            DateTime curDataTime = dateTime;
            for (int i = 0; i < 80; i++) {
                JSONObject messageObj = new JSONObject();
                messageObj.put("dataCode", dataCode);
                messageObj.put("typeCode", PredictedTypeEnum.MULTI.getCode());
                messageObj.put("time", dateTime.toString("yyyy-MM-dd HH:mm:ss"));
                messageObj.put("itemVal", (Double) (Math.random() * 100));
                messageObj.put("unit", "℃");
                messageArray.add(messageObj);

                curDataTime = curDataTime.offsetNew(DateField.MINUTE, 1);
            }
        }
        predictedDataService.handleMqttMessage(topic, messageArray.toString());
    }

    private void createSingleData(String topic, List<String> dataCodeList, DateTime dateTime) {
        JSONArray messageArray = new JSONArray();
        for (String dataCode : dataCodeList) {
            for (PredictedTypeEnum typeEnum : PredictedTypeEnum.getSingleTypeList()) {
                JSONObject messageObj = new JSONObject();
                messageObj.put("dataCode", dataCode);
                messageObj.put("typeCode", typeEnum.getCode());
                messageObj.put("time", handleTime(dateTime, typeEnum));
                messageObj.put("itemVal", (Double) (Math.random() * 100));
                messageObj.put("unit", "℃");
                messageArray.add(messageObj);
            }
        }
        predictedDataService.handleMqttMessage(topic, messageArray.toString());
    }

    private String handleTime(DateTime dateTime, PredictedTypeEnum typeEnum) {
        switch (typeEnum) {
            case SINGLE_T20:
                return dateTime.offsetNew(DateField.MINUTE, 20).toString("yyyy-MM-dd HH:mm:ss");
            case SINGLE_T40:
                return dateTime.offsetNew(DateField.MINUTE, 40).toString("yyyy-MM-dd HH:mm:ss");
            case SINGLE_T60:
                return dateTime.offsetNew(DateField.MINUTE, 60).toString("yyyy-MM-dd HH:mm:ss");
            case SINGLE_T80:
                return dateTime.offsetNew(DateField.MINUTE, 80).toString("yyyy-MM-dd HH:mm:ss");
            case SINGLE_T27:
                return dateTime.offsetNew(DateField.MINUTE, 27).toString("yyyy-MM-dd HH:mm:ss");
            case SINGLE_T54:
                return dateTime.offsetNew(DateField.MINUTE, 54).toString("yyyy-MM-dd HH:mm:ss");
            default:
                return null;
        }
    }
}
