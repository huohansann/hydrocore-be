package com.siact.module.control.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.constant.ConstantNum;
import com.siact.module.control.dto.ControlSettingGasDTO;
import com.siact.module.control.entity.ControlSettingGasEntity;
import com.siact.module.control.mapper.ControlSettingGasMapper;
import com.siact.module.control.service.ControlSettingGasService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class ControlSettingGasServiceImpl extends ServiceImpl<ControlSettingGasMapper, ControlSettingGasEntity> implements ControlSettingGasService {


    // 获取有效的数据
    @Override
    public List<ControlSettingGasEntity> getValidList() {
        LambdaQueryWrapper<ControlSettingGasEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ControlSettingGasEntity::getDeleteFlag, ConstantNum.ZERO_INT);

        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public void deleteByDataCode(List<String> publishGasDataCodeList) {
        LambdaUpdateWrapper<ControlSettingGasEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ControlSettingGasEntity::getDeleteFlag, ConstantNum.ZERO_INT);
        updateWrapper.in(ControlSettingGasEntity::getDataCode, publishGasDataCodeList);
        // 更改删除状态
        updateWrapper.set(ControlSettingGasEntity::getDeleteFlag, ConstantNum.NUMBER_ONE);
        updateWrapper.set(ControlSettingGasEntity::getUpdateTime, new Date());
        update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGasSetting(List<ControlSettingGasDTO> publishGasSettingList) {
        if (ObjectUtils.isEmpty(publishGasSettingList)) {
            return;
        }

        ArrayList<ControlSettingGasEntity> addDataList = new ArrayList<>();
        for (ControlSettingGasDTO publishGasSetting : publishGasSettingList) {
            ControlSettingGasEntity gasEntity = new ControlSettingGasEntity();
            gasEntity.setNumber(publishGasSetting.getNumber());
            gasEntity.setDataCode(publishGasSetting.getDataCode());
            gasEntity.setGasPublishCodes("");// TODO 配置到数据字典
            BigDecimal gasAlgorithmCalcVal = publishGasSetting.getGasAlgorithmCalcVal() == null ? null : BigDecimal.valueOf(publishGasSetting.getGasAlgorithmCalcVal());
            gasEntity.setGasAlgorithmCalcVal(gasAlgorithmCalcVal);
            BigDecimal gasManualVal = publishGasSetting.getGasManualVal() == null ? null : BigDecimal.valueOf(publishGasSetting.getGasManualVal());
            gasEntity.setGasManualVal(gasManualVal);
            gasEntity.setAutoState(publishGasSetting.getState());
            gasEntity.setDeleteFlag(ConstantNum.ZERO_INT);
            gasEntity.setCreateTime(new Date());
            gasEntity.setUpdateTime(new Date());
            addDataList.add(gasEntity);
        }
        saveBatch(addDataList);
    }
}
