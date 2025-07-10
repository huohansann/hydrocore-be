package com.siact.module.control.validator;

import com.alibaba.fastjson2.JSON;
import com.siact.common.utils.ConvertUtils;
import com.siact.module.base.dto.KilnInfoDistributeDTO;
import com.siact.module.control.entity.ControlRuleEntity;
import com.siact.module.control.enums.ControlRuleTypeEnum;
import com.siact.module.control.service.ControlRuleService;
import com.siact.module.control.vo.ControlRuleVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 根据约束规则校验数据合法性
 */
@Slf4j
@Order(1)
@Component
@ConditionalOnProperty(name = "rule.validator.controlRule.enable", havingValue = "true", matchIfMissing = true)
public class ControlRuleValidator implements RuleValidator {

    @Autowired
    private ControlRuleService controlRuleService;

    @Autowired
    private ControlRuleValidatorTypeUtil controlRuleUtil;

    @Override
    public RuleValidateResult validate(List<KilnInfoDistributeDTO> kilnInfoList) {

        List<String> errors = new ArrayList<>();
        // 1: 获取所有的约束规则 (查询所有类型) ps: 已经设置过了换火  液压  炉压 的合法状态
        List<ControlRuleEntity> ruleEntityList = controlRuleService.queryRuleByTypes(null);
        List<ControlRuleVO> ruleVOList = ConvertUtils.sourceToTarget(ruleEntityList, ControlRuleVO.class);

        // 先校验换火、液位、炉压状态
        for (ControlRuleVO ruleVO : ruleVOList) {
            log.info("校验换火、液位、炉压状态:{}", JSON.toJSONString(ruleVO));
            controlRuleUtil.validateFireAndLiquidAndPressure(ruleVO, errors);
        }
        if (CollectionUtils.isNotEmpty(errors)) {
            return RuleValidateResult.fail(errors);
        }


        Map<String, BigDecimal> gasSettingDataValMap = new HashMap<>();

        for (KilnInfoDistributeDTO kilnInfo : kilnInfoList) {
            gasSettingDataValMap.put(kilnInfo.getDataCode(), kilnInfo.getGasVal());
        }

        if (ObjectUtils.isEmpty(gasSettingDataValMap)) {
            log.error("气量设定值为空!");
            return RuleValidateResult.fail("气量设定值为空!");
        }

        for (ControlRuleVO ruleVO : ruleVOList) {
            Integer type = ruleVO.getType();
            if (ControlRuleTypeEnum.STEP.getCode().equals(type)) {
                // 校验调节步长
                log.info("校验调节步长:{}", JSON.toJSONString(ruleVO));
                controlRuleUtil.validateStep(kilnInfoList, ruleVO, errors);
            } else if (ControlRuleTypeEnum.TOTAL_GAS.getCode().equals(type) || ControlRuleTypeEnum.DIFF_GAS.getCode().equals(type)) {
                // 校验总气量 和 气量差值
                log.info("校验总气量 和 气量差值:{}", JSON.toJSONString(ruleVO));
                controlRuleUtil.validateTotalGasAndDiffGas(ruleVO, errors, gasSettingDataValMap);
            }

        }

        if (CollectionUtils.isNotEmpty(errors)) {
            return RuleValidateResult.fail(errors);
        }
        return RuleValidateResult.pass();
    }

}
