package com.siact.module.model.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.module.model.entity.ModelPublishRecordEntity;
import com.siact.module.model.mapper.ModelPublishRecordMapper;
import com.siact.module.model.service.ModelPublishRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ModelPublishRecordServiceImpl extends ServiceImpl<ModelPublishRecordMapper, ModelPublishRecordEntity> implements ModelPublishRecordService {
    @Override
    @Transactional
    public void saveModelPublishRecord(List<ModelPublishRecordEntity> publishRecordList) {
        if (ObjectUtil.isEmpty(publishRecordList)) {
            return;
        }

        saveBatch(publishRecordList);
    }
}
