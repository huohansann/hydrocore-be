package com.siact.module.base.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.siact.common.constant.ConstantField;
import com.siact.common.exception.CustomException;
import com.siact.common.utils.ConvertUtils;
import com.siact.module.base.dto.GasOperationDTO;
import com.siact.module.base.dto.RuleAddDTO;
import com.siact.module.base.dto.TempConditionDTO;
import com.siact.module.base.entity.GasOperationEntity;
import com.siact.module.base.entity.RuleMetaEntity;
import com.siact.module.base.entity.TempConditionEntity;
import com.siact.module.base.mapper.GasOperationMapper;
import com.siact.module.base.mapper.RuleMetaMapper;
import com.siact.module.base.mapper.TempConditionMapper;
import com.siact.module.base.service.IRuleService;
import com.siact.module.base.service.TplService;
import com.siact.module.base.vo.RuleDetailVO;
import com.siact.module.base.vo.TplVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RuleServiceImpl implements IRuleService {
    @Autowired
    private RuleMetaMapper ruleMetaMapper;
    @Autowired
    private TempConditionMapper tempConditionMapper;
    @Autowired
    private GasOperationMapper gasOperationMapper;

    @Autowired
    private TplService tplService;

    @Override
    public List<RuleDetailVO> listRules() {
        List<RuleMetaEntity> metaList = ruleMetaMapper.selectList(new LambdaQueryWrapper<RuleMetaEntity>().eq(RuleMetaEntity::getStatus, 0));
        if (CollectionUtils.isEmpty(metaList)) {
            return Collections.emptyList();
        }

        // 1. 获取所有ruleCode
        List<String> ruleCodes = metaList.stream().map(RuleMetaEntity::getRuleCode).collect(Collectors.toList());

        // 2. 一次查出所有子表
        List<TempConditionEntity> tempList = tempConditionMapper.selectList(new LambdaQueryWrapper<TempConditionEntity>().in(TempConditionEntity::getRuleCode, ruleCodes));
        List<GasOperationEntity> gasList = gasOperationMapper.selectList(new LambdaQueryWrapper<GasOperationEntity>().in(GasOperationEntity::getRuleCode, ruleCodes));

        // 3. 按ruleCode分组
        Map<String, List<TempConditionDTO>> tempMap = tempList.stream()
            .map(e -> ConvertUtils.sourceToTarget(e, TempConditionDTO.class))
            .collect(Collectors.groupingBy(TempConditionDTO::getRuleCode));
        Map<String, List<GasOperationDTO>> gasMap = gasList.stream()
            .map(e -> ConvertUtils.sourceToTarget(e, GasOperationDTO.class))
            .collect(Collectors.groupingBy(GasOperationDTO::getRuleCode));

        // 4. 组装VO
        return metaList.stream().map(meta -> {
            RuleDetailVO vo = ConvertUtils.sourceToTarget(meta, RuleDetailVO.class);
            vo.setTempConditions(tempMap.getOrDefault(meta.getRuleCode(), Collections.emptyList()));
            vo.setGasOperations(gasMap.getOrDefault(meta.getRuleCode(), Collections.emptyList()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(RuleAddDTO dto) {
        String ruleCode = dto.getRuleCode();
        boolean isUpdate = false;
        if (ruleCode != null && !ruleCode.isEmpty()) {
            // 判断是否存在
            RuleMetaEntity exist = ruleMetaMapper.selectOne(new LambdaQueryWrapper<RuleMetaEntity>().eq(RuleMetaEntity::getRuleCode, ruleCode));
            if (exist != null) {
                // 更新主表
                exist.setRuleName(dto.getRuleName());
                if (dto.getStatus() != null) {
                    exist.setStatus(dto.getStatus());
                }
                ruleMetaMapper.updateById(exist);
                isUpdate = true;
            }
        }
        if (!isUpdate) {
            // 新增主表
            ruleCode = UUID.randomUUID().toString().replace("-", "");
            RuleMetaEntity meta = new RuleMetaEntity();
            meta.setRuleCode(ruleCode);
            meta.setRuleName(dto.getRuleName());
            meta.setStatus(0);
            int insertMeta = ruleMetaMapper.insert(meta);
            if (insertMeta <= 0) {
                return false;
            }
        }
        // 先删子表
        tempConditionMapper.delete(new LambdaQueryWrapper<TempConditionEntity>().eq(TempConditionEntity::getRuleCode, ruleCode));
        gasOperationMapper.delete(new LambdaQueryWrapper<GasOperationEntity>().eq(GasOperationEntity::getRuleCode, ruleCode));
        // 再插子表
        String finalRuleCode = ruleCode;
        if (dto.getTempConditions() != null && !dto.getTempConditions().isEmpty()) {
            List<TempConditionEntity> tempEntities = ConvertUtils.sourceToTarget(dto.getTempConditions(), TempConditionEntity.class);
            tempEntities.forEach(e -> e.setRuleCode(finalRuleCode));
            tempConditionMapper.insertBatch(tempEntities);
        }
        if (dto.getGasOperations() != null && !dto.getGasOperations().isEmpty()) {
            List<GasOperationEntity> gasEntities = ConvertUtils.sourceToTarget(dto.getGasOperations(), GasOperationEntity.class);
            gasEntities.forEach(e -> e.setRuleCode(finalRuleCode));
            gasOperationMapper.insertBatch(gasEntities);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(String ruleCode) {
        // 先删子表
        tempConditionMapper.delete(new LambdaQueryWrapper<TempConditionEntity>().eq(TempConditionEntity::getRuleCode, ruleCode));
        gasOperationMapper.delete(new LambdaQueryWrapper<GasOperationEntity>().eq(GasOperationEntity::getRuleCode, ruleCode));
        // 再删主表
        int metaDel = ruleMetaMapper.delete(new LambdaQueryWrapper<RuleMetaEntity>().eq(RuleMetaEntity::getRuleCode, ruleCode));
        log.info("删除条规：{}，主表结果：{}", ruleCode, metaDel);
        return metaDel > 0;
    }

    @Override
    public RuleDetailVO detail(String ruleCode) {
        RuleMetaEntity meta =
                ruleMetaMapper.selectOne(new LambdaQueryWrapper<RuleMetaEntity>().eq(RuleMetaEntity::getRuleCode, ruleCode));
        if (meta == null) {
            throw new CustomException("规则不存在");
        }
        RuleDetailVO vo = ConvertUtils.sourceToTarget(meta, RuleDetailVO.class);
        List<TempConditionEntity> tempList =
                tempConditionMapper.selectList(new LambdaQueryWrapper<TempConditionEntity>().eq(TempConditionEntity::getRuleCode, ruleCode));
        vo.setTempConditions(ConvertUtils.sourceToTarget(tempList, TempConditionDTO.class));
        List<GasOperationEntity> gasList =
                gasOperationMapper.selectList(new LambdaQueryWrapper<GasOperationEntity>().eq(GasOperationEntity::getRuleCode, ruleCode));
        vo.setGasOperations(ConvertUtils.sourceToTarget(gasList, GasOperationDTO.class));
        return vo;
    }

    @Override
    public JSONObject table() {
        TplVO tplVO = tplService.selectTplByCode(ConstantField.RULES_HEADERS);
        if (tplVO == null) {
            throw new CustomException("表头模板不存在");
        }
        JSONArray jsonArray = JSONArray.parseArray(tplVO.getTplContent());
        JSONObject result = new JSONObject();
        result.put(ConstantField.HEADERS, jsonArray);
        result.put(ConstantField.DATALIST, listRules());
        return result;
    }
} 