package com.siact.module.predicted.task;

import com.alibaba.fastjson2.JSON;
import com.siact.module.enmus.PublishStatusEnum;
import com.siact.module.model.entity.ModelInfoEntity;
import com.siact.module.model.entity.ModelPublishRecordEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AlgorithmTask {

    public void run() {

        // 1:读取模型数据当中的预测相关的模型

        // 2:解析模型  组装param

        // 3:解析预测结果




        // TODO 2:调用算法的接口
        // 新增模型下发调用算法的记录
//        List<ModelPublishRecordEntity> publishRecordList = new ArrayList<>();
//        for (ModelInfoEntity modelEntity : selectedModelInfoList) {
//            ModelPublishRecordEntity publishRecord = new ModelPublishRecordEntity();
//            publishRecord.setModelInfoId(modelEntity.getId());
//            publishRecord.setDataCode(modelEntity.getDataCode());
//            publishRecord.setPredictedType(modelEntity.getPredictedType());
//            publishRecord.setPredictedTypeCode(modelEntity.getPredictedTypeCode());
//            publishRecord.setPublishParam(JSON.toJSONString(publishParam));
//            publishRecord.setModelCode(modelEntity.getModelCode());
//            publishRecord.setStatus(PublishStatusEnum.PUBLISHING.getCode());
//            publishRecord.setCreateTime(new Date());
//            publishRecord.setPublishInfoId(publishInfoId);
//            publishRecordList.add(publishRecord);
//        }
//        // 3: 保存发布记录 (ps:后续通过回调或者mqtt 更新发布记录状态)
//        modelPublishRecordService.saveModelPublishRecord(publishRecordList);
    }
}
