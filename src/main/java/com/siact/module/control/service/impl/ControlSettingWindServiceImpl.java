package com.siact.module.control.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.constant.ConstantNum;
import com.siact.module.control.mapper.ControlSettingWindMapper;
import com.siact.module.control.dto.ControlSettingWindDTO;
import com.siact.module.control.entity.ControlSettingWindEntity;
import com.siact.module.control.service.ControlSettingWindService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class ControlSettingWindServiceImpl extends ServiceImpl<ControlSettingWindMapper, ControlSettingWindEntity> implements ControlSettingWindService {

    // 获取有效的数据
    @Override
    public List<ControlSettingWindEntity> getValidList() {
        LambdaQueryWrapper<ControlSettingWindEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ControlSettingWindEntity::getDeleteFlag, ConstantNum.ZERO_INT);

        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public void deleteByDataCode(List<String> publishWindDataCodeList) {
        LambdaUpdateWrapper<ControlSettingWindEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ControlSettingWindEntity::getDeleteFlag, ConstantNum.ZERO_INT);
        updateWrapper.in(ControlSettingWindEntity::getDataCode, publishWindDataCodeList);
        // 更改删除状态
        updateWrapper.set(ControlSettingWindEntity::getDeleteFlag, ConstantNum.NUMBER_ONE);
        updateWrapper.set(ControlSettingWindEntity::getUpdateTime, new Date());
        update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWindSetting(List<ControlSettingWindDTO> publishWindSettingList) {
        if (ObjectUtils.isEmpty(publishWindSettingList)) {
            return;
        }

        ArrayList<ControlSettingWindEntity> addDataList = new ArrayList<>();
        for (ControlSettingWindDTO publishWindSetting : publishWindSettingList) {
            ControlSettingWindEntity windEntity = new ControlSettingWindEntity();
            windEntity.setNumber(publishWindSetting.getNumber());
            windEntity.setDataCode(publishWindSetting.getDataCode());
            windEntity.setRatePublishCodes("");// TODO 配置到数据字典
            windEntity.setRateManualVal(publishWindSetting.getRateManualVal());
            windEntity.setDeleteFlag(ConstantNum.ZERO_INT);
            windEntity.setCreateTime(new Date());
            windEntity.setUpdateTime(new Date());
        }
        saveBatch(addDataList);
    }
}
