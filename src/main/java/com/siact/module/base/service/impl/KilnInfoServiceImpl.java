package com.siact.module.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.utils.ConvertUtils;
import com.siact.module.base.dto.KilnInfoDTO;
import com.siact.module.base.dto.KilnInfoDistributeDTO;
import com.siact.module.base.dto.KilnInfoGasFlowDTO;
import com.siact.module.base.dto.KilnInfoQuery;
import com.siact.module.base.dto.KilnInfoWindDisDTO;
import com.siact.module.base.entity.KilnInfoEntity;
import com.siact.module.base.mapper.KilnInfoMapper;
import com.siact.module.base.service.IKilnInfoService;
import com.siact.module.base.vo.KilnInfoVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 炉子基本信息配置 服务实现
 */
@Service
public class KilnInfoServiceImpl extends ServiceImpl<KilnInfoMapper, KilnInfoEntity> implements IKilnInfoService {
    @Autowired
    private KilnInfoMapper kilnInfoMapper;

    @Override
    public KilnInfoVO selectKilnInfoById(Long id) {
        KilnInfoEntity entity = kilnInfoMapper.selectById(id);
        if (entity == null) {
            return null;
        }
        KilnInfoVO vo = new KilnInfoVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public List<KilnInfoVO> selectKilnInfoList(KilnInfoQuery query) {
        LambdaQueryWrapper<KilnInfoEntity> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(StringUtils.isNoneBlank(query.getNumber()), KilnInfoEntity::getNumber, query.getNumber());
        wrapper.eq(StringUtils.isNoneBlank(query.getCode()), KilnInfoEntity::getCode, query.getCode());

        if (query.getState() != null) {
            wrapper.eq(KilnInfoEntity::getState, query.getState());
        }

        List<KilnInfoEntity> list = list(wrapper);
        return ConvertUtils.sourceToTarget(list, KilnInfoVO.class);
    }


    @Override
    public int insertKilnInfo(KilnInfoDTO dto) {
        KilnInfoEntity entity = new KilnInfoEntity();
        BeanUtils.copyProperties(dto, entity);
        return kilnInfoMapper.insert(entity);
    }

    @Override
    public int updateKilnInfo(KilnInfoDTO dto) {
        KilnInfoEntity kilnInfoEntity = ConvertUtils.sourceToTarget(dto, KilnInfoEntity.class);
        return kilnInfoMapper.updateById(kilnInfoEntity);
    }

    @Override
    public int deleteKilnInfoByIds(Long[] ids) {
        int count = 0;
        for (Long id : ids) {
            count += kilnInfoMapper.deleteById(id);
        }
        return count;
    }

    @Override
    public int saveKilnInfoBatch(List<KilnInfoDTO> list) {
        List<KilnInfoEntity> kilnInfoEntities = ConvertUtils.sourceToTarget(list, KilnInfoEntity.class);
        saveOrUpdateBatch(kilnInfoEntities);
        return kilnInfoEntities.size();
    }

    @Override
    public int updateDistribute(List<KilnInfoDistributeDTO> list) {
        List<KilnInfoEntity> kilnInfoEntities = ConvertUtils.sourceToTarget(list, KilnInfoEntity.class);
        baseMapper.updateDistributeBatch(kilnInfoEntities);
        return kilnInfoEntities.size();
    }

    @Override
    public int updateGasFlow(List<KilnInfoGasFlowDTO> list) {
        List<KilnInfoEntity> kilnInfoEntities = ConvertUtils.sourceToTarget(list, KilnInfoEntity.class);
        baseMapper.updateGasFlowBatch(kilnInfoEntities);
        return kilnInfoEntities.size();
    }

    @Override
    public int updateWindDis(List<KilnInfoWindDisDTO> list) {
        List<KilnInfoEntity> kilnInfoEntities = ConvertUtils.sourceToTarget(list, KilnInfoEntity.class);
        baseMapper.updateWindDisBatch(kilnInfoEntities);
        return kilnInfoEntities.size();
    }

} 