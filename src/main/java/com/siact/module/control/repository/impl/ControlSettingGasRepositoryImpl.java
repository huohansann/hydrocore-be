package com.siact.module.control.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.siact.common.constant.ConstantNum;
import com.siact.common.repository.BaseRepositoryImpl;
import com.siact.module.control.convert.ControlSettingGasConvert;
import com.siact.module.control.dto.ControlSettingGasDTO;
import com.siact.module.control.entity.ControlSettingGasEntity;
import com.siact.module.control.mapper.ControlSettingGasMapper;
import com.siact.module.control.repository.ControlSettingGasRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-04 14:07
 * @className : ControlSettingGasRepositoryImpl
 * @description : 天然气控制数据持久层实现
 */
@RequiredArgsConstructor
@Repository
public class ControlSettingGasRepositoryImpl extends BaseRepositoryImpl<ControlSettingGasMapper, ControlSettingGasEntity> implements ControlSettingGasRepository {
    private final ControlSettingGasMapper mapper;
    private final ControlSettingGasConvert convert;

    /**
     * 获取有效的天然气设置数据
     */
    @Override
    public List<ControlSettingGasEntity> queryValid() {
        LambdaQueryWrapper<ControlSettingGasEntity> wrapper = Wrappers.<ControlSettingGasEntity>lambdaQuery().eq(ControlSettingGasEntity::getDeleteFlag, ConstantNum.ZERO_INT);
        return mapper.selectList(wrapper);
    }

    /**
     * 根据 dataCode 删除天然气设置数据(逻辑删除)
     */
    @Override
    public Boolean deleteByDataCode(List<String> dataCodes) {
        LambdaUpdateWrapper<ControlSettingGasEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ControlSettingGasEntity::getDeleteFlag, ConstantNum.ZERO_INT);
        updateWrapper.in(ControlSettingGasEntity::getDataCode, dataCodes);
        // 更改删除状态
        updateWrapper.set(ControlSettingGasEntity::getDeleteFlag, ConstantNum.NUMBER_ONE);
        updateWrapper.set(ControlSettingGasEntity::getUpdateTime, new Date());
        return SqlHelper.retBool(mapper.update(null, updateWrapper));
    }

    /**
     * 批量保存天然气设置
     */
    @Override
    public Boolean save(List<ControlSettingGasDTO> publishList) {
        if (ObjectUtils.isEmpty(publishList)) return true;
        List<ControlSettingGasEntity> entities = new ArrayList<>();

        for (ControlSettingGasDTO dto : publishList) {
            ControlSettingGasEntity entity = convert.toEntity(dto);
            entity.setGasPublishCodes(""); // TODO 配置到数据字典
            entity.setDeleteFlag(ConstantNum.ZERO_INT);
            entity.setCreateTime(new Date());
            entity.setUpdateTime(new Date());
            entities.add(entity);
        }
        return saveBatch(entities);
    }
}
