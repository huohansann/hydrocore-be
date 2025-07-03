package com.siact.module.model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.constant.ConstantNum;
import com.siact.module.model.entity.ModelPublishInfoEntity;
import com.siact.module.model.mapper.ModelPublishInfoMapper;
import com.siact.module.model.service.ModelPublishInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ModelPublishInfoServiceImpl extends ServiceImpl<ModelPublishInfoMapper, ModelPublishInfoEntity> implements ModelPublishInfoService {
    @Override
    public void removeByDataCode(String dataCode) {
        LambdaUpdateWrapper<ModelPublishInfoEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ModelPublishInfoEntity::getDataCode, dataCode);
        wrapper.set(ModelPublishInfoEntity::getDeleted, ConstantNum.NUMBER_ONE);
        update(wrapper);
    }

    @Override
    public ModelPublishInfoEntity queryLastPublishInfoByDataCode(String dataCode) {

        LambdaQueryWrapper<ModelPublishInfoEntity> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(ModelPublishInfoEntity::getDataCode, dataCode);
        queryWrapper.eq(ModelPublishInfoEntity::getDeleted, ConstantNum.ZERO_INT);

        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public List<ModelPublishInfoEntity> queryLastPublishInfoByDataCodeList(List<String> dataCodeList) {

        LambdaQueryWrapper<ModelPublishInfoEntity> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.in(ModelPublishInfoEntity::getDataCode, dataCodeList);
        queryWrapper.eq(ModelPublishInfoEntity::getDeleted, ConstantNum.ZERO_INT);

        return baseMapper.selectList(queryWrapper);
    }
}
