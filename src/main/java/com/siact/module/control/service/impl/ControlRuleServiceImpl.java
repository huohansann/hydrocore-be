package com.siact.module.control.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.constant.ConstantNum;
import com.siact.common.utils.ConvertUtils;
import com.siact.module.control.dto.ControlRuleDTO;
import com.siact.module.control.dto.ControlRuleQuery;
import com.siact.module.control.entity.ControlRuleEntity;
import com.siact.module.control.enums.ControlRuleTypeEnum;
import com.siact.module.control.mapper.ControlRuleMapper;
import com.siact.module.control.service.ControlRuleService;
import com.siact.module.control.vo.ControlRuleVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ControlRuleServiceImpl extends ServiceImpl<ControlRuleMapper, ControlRuleEntity> implements ControlRuleService {
    @Override
    public ControlRuleVO selectControlRuleById(Long id) {
        ControlRuleEntity entity = this.getById(id);
        return ConvertUtils.sourceToTarget(entity, ControlRuleVO.class);
    }

    @Override
    public List<ControlRuleVO> selectControlRuleList(ControlRuleQuery query) {
        LambdaQueryWrapper<ControlRuleEntity> wrapper = new LambdaQueryWrapper<>();

        wrapper.in(ObjectUtils.isNotEmpty(query.getTypes()), ControlRuleEntity::getType, query.getTypes());
        wrapper.eq(ControlRuleEntity::getStatus, ConstantNum.NUMBER_ONE);

        List<ControlRuleEntity> list = this.list(wrapper);

        List<ControlRuleVO> rtnDto = list.stream().map(e -> ConvertUtils.sourceToTarget(e, ControlRuleVO.class)).collect(Collectors.toList());
        // 校验每一条约束规则  是否合法
        for (ControlRuleVO rule : rtnDto) {
            // 查询 换火 液位 炉压是否异常的状态 TODO 目前点位还没有对接 对接后需要完善逻辑
            Integer type = rule.getType();
            if (ControlRuleTypeEnum.FIRE.getCode().equals(type)) {
                rule.setLegal(true);
            }else if (ControlRuleTypeEnum.LIQUID.getCode().equals(type)) {
                rule.setLegal(true);
            }else if (ControlRuleTypeEnum.PRESSURE.getCode().equals(type)) {
                rule.setLegal(true);
            }
        }

        return rtnDto;
    }

    @Override
    public int insertControlRule(ControlRuleDTO dto) {
        ControlRuleEntity entity = ConvertUtils.sourceToTarget(dto, ControlRuleEntity.class);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        return this.save(entity) ? 1 : 0;
    }

    @Override
    public int updateControlRule(List<ControlRuleDTO> dtoList) {
        List<ControlRuleEntity> entities =
                ConvertUtils.sourceToTarget(dtoList, ControlRuleEntity.class);

        Date updateTime = new Date();
        entities.forEach(e -> {
            e.setUpdateTime(updateTime);
        });
        return this.updateBatchById(entities) ? 1 : 0;
    }

    @Override
    public int deleteControlRuleByIds(Long[] ids) {
        return this.removeByIds(java.util.Arrays.asList(ids)) ? 1 : 0;
    }

    @Override
    public Boolean legal(ControlRuleQuery query) {
        List<ControlRuleVO> ruleVOS = selectControlRuleList(query);

        if (ObjectUtils.isEmpty(ruleVOS)) {
            log.info("未查询到约束规则,允许修改数据");
            return true;
        }

        List<Boolean> legalList = ruleVOS.stream().map(ControlRuleVO::getLegal).filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());

        return !legalList.contains(false);
    }
}
