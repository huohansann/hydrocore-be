package com.siact.module.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.utils.ConvertUtils;
import com.siact.module.base.dto.ControlIntervalConfigDTO;
import com.siact.module.base.entity.ControlIntervalConfigEntity;
import com.siact.module.base.mapper.ControlIntervalConfigMapper;
import com.siact.module.base.service.ControlIntervalConfigService;
import com.siact.module.base.vo.ControlIntervalConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 控制区间设置
 *
 * @author wr
 */
@Service
@Slf4j
public class ControlIntervalConfigServiceImpl extends ServiceImpl<ControlIntervalConfigMapper, ControlIntervalConfigEntity> implements ControlIntervalConfigService {
    @Override
    public List<ControlIntervalConfigDTO> selectListByCondition(ControlIntervalConfigVO configVO) {
        LambdaQueryWrapper<ControlIntervalConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNoneBlank(configVO.getMeasurePoint()),ControlIntervalConfigEntity::getMeasurePoint, configVO.getMeasurePoint());
        wrapper.eq(StringUtils.isNoneBlank(configVO.getPointType()),ControlIntervalConfigEntity::getPointType, configVO.getPointType());
        // 没有条件就默认查询全部
        List<ControlIntervalConfigEntity> controlIntervalConfigEntities = baseMapper.selectList(wrapper);
        return ConvertUtils.sourceToTarget(controlIntervalConfigEntities, ControlIntervalConfigDTO.class);
    }

    @Override
    public List<ControlIntervalConfigDTO> selectListByDataCodeList(List<String> dataCodeList) {
        if (ObjectUtils.isEmpty(dataCodeList)) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<ControlIntervalConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ControlIntervalConfigEntity::getDataCode, dataCodeList);

        List<ControlIntervalConfigEntity> controlIntervalConfigEntities = baseMapper.selectList(wrapper);
        return ConvertUtils.sourceToTarget(controlIntervalConfigEntities, ControlIntervalConfigDTO.class);
    }

    @Override
    public void add(ControlIntervalConfigDTO configDTO) {
        ControlIntervalConfigEntity configEntity = ConvertUtils.sourceToTarget(configDTO, ControlIntervalConfigEntity.class);
        baseMapper.insert(configEntity);
    }

    @Override
    public void updateConfig(ControlIntervalConfigDTO configDTO) {
        ControlIntervalConfigEntity configEntity = ConvertUtils.sourceToTarget(configDTO, ControlIntervalConfigEntity.class);
        baseMapper.updateById(configEntity);
    }

    @Override
    public ControlIntervalConfigDTO get(ControlIntervalConfigVO configVO) {
        if (configVO.getId() != null) {
            // 优先主键查询
            return ConvertUtils.sourceToTarget(baseMapper.selectById(configVO.getId()), ControlIntervalConfigDTO.class);
        }
        LambdaQueryWrapper<ControlIntervalConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNoneBlank(configVO.getMeasurePoint()),ControlIntervalConfigEntity::getMeasurePoint, configVO.getMeasurePoint());
        wrapper.eq(StringUtils.isNoneBlank(configVO.getPointType()),ControlIntervalConfigEntity::getPointType, configVO.getPointType());
        ControlIntervalConfigEntity configEntity = baseMapper.selectOne(wrapper);
        return ConvertUtils.sourceToTarget(configEntity, ControlIntervalConfigDTO.class);
    }
}
