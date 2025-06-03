package com.siact.module.model.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.model.entity.ModelPublishRecordEntity;

import java.util.List;

public interface ModelPublishRecordService extends IService<ModelPublishRecordEntity> {
    void saveModelPublishRecord(List<ModelPublishRecordEntity> publishRecordList);
}
