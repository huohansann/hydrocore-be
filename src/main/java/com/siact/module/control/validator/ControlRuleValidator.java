package com.siact.module.control.validator;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.siact.common.constant.ConstantBase;
import com.siact.common.utils.JepUtils;
import com.siact.module.base.dto.KilnInfoDistributeDTO;
import com.siact.module.control.dto.ControlRuleQuery;
import com.siact.module.control.enums.ControlRuleTypeEnum;
import com.siact.module.control.service.ControlRuleService;
import com.siact.module.control.vo.ControlRuleVO;
import com.siact.sec.dto.IntervalValParamsDto;
import com.siact.sec.sevice.DataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 根据约束规则校验数据合法性
 */
@Slf4j
@Order(3)
@Component
public class ControlRuleValidator implements RuleValidator {

    @Autowired
    private ControlRuleService controlRuleService;

    @Autowired
    private DataService dataService;

    @Override
    public RuleValidateResult validate(List<KilnInfoDistributeDTO> list) {

        List<HashMap<String, Object>> errors = new ArrayList<>();
        // 1: 获取所有的约束规则 (查询所有类型)
        List<ControlRuleVO> ruleVOList = controlRuleService.selectControlRuleList(new ControlRuleQuery());

        Set<String> allGasSettingCodeList = new HashSet<>();
        for (ControlRuleVO ruleVO : ruleVOList) {
            if (ObjectUtils.isNotEmpty(ruleVO.getFormula())) {
                allGasSettingCodeList.addAll(getFormulaDataCode(ruleVO.getFormula()));
            }
            if (ObjectUtils.isNotEmpty(ruleVO.getCompareFormula())) {
                allGasSettingCodeList.addAll(getFormulaDataCode(ruleVO.getCompareFormula()));
            }
        }

        // 查询1# ~ 8# 的所有气量设定值
        IntervalValParamsDto querySecDataParam = new IntervalValParamsDto();
        querySecDataParam.setDataCodes(new ArrayList<>(allGasSettingCodeList));
        querySecDataParam.setStartTime("");
        querySecDataParam.setEndTime("");
        querySecDataParam.setCalcType(ConstantBase.LAST);

        JSONObject gasSettingDataValJsonObj = dataService.queryBetweenVal(querySecDataParam);

        Map<String, BigDecimal> gasSettingDataValMap =
                ObjectUtils.isEmpty(gasSettingDataValJsonObj) ? new HashMap<>() : com.alibaba.fastjson2.JSONObject.parseObject(gasSettingDataValJsonObj.toJSONString(), new TypeReference<Map<String, BigDecimal>>() {
                });

        if (ObjectUtils.isEmpty(gasSettingDataValMap)) {
            errors.add(new HashMap<String, Object>() {{
                log.error("查询孪生数据失败!");
                put("查询孪生数据失败!", "");
            }});
            return RuleValidateResult.fail(errors);
        }

        for (ControlRuleVO ruleVO : ruleVOList) {
            Integer type = ruleVO.getType();
            if (ControlRuleTypeEnum.STEP.getCode().equals(type)) {
                // 校验调节步长
                for (KilnInfoDistributeDTO kilnInfoDistributeDTO : list) {
                    // 调节变动值
                    HashMap<String, BigDecimal> paramValMap = new HashMap<>();
                    paramValMap.put("step", kilnInfoDistributeDTO.getGasValueChange());
                    String validFormula =  getValidFormula(ruleVO, errors);
                    Boolean result = JepUtils.calcBoolean(validFormula, paramValMap, false, null);
                    ruleVO.setLegal(result);
                }
            } else if (ControlRuleTypeEnum.TOTAL_GAS.getCode().equals(type) || ControlRuleTypeEnum.DIFF_GAS.getCode().equals(type)) {
                // 校验气量总和 或者 校验气量差 根据公式进行计算
                // 组装校验公式
                String validFormula = getValidFormula(ruleVO, errors);
                Boolean result = JepUtils.calcBoolean(validFormula, gasSettingDataValMap, false, null);
                ruleVO.setLegal(result);
            }

            if (!ruleVO.getLegal()) {
                errors.add(new HashMap<String, Object>() {{
                    log.error("未通过校验!规则:{}", JSON.toJSONString(ruleVO));
                    String errorMsg = buildErrorMsg(ruleVO);
                    put("未通过校验!规则:", errorMsg);
                }});
            }

        }


        if (CollectionUtils.isNotEmpty(errors)) {
            return RuleValidateResult.fail(errors);
        }
        return RuleValidateResult.pass();
    }

    private Collection<String> getFormulaDataCode(String formula) {
        // formula 根据运算符号进行分隔
        return Arrays.asList(formula.split("\\+|-|\\*|/"));
    }

    @NotNull
    private static String buildErrorMsg(ControlRuleVO ruleVO) {
        CharSequence[] charSequences = {ruleVO.getFormulaDesc() + ruleVO.getSymbol() + ruleVO.getCompareValue(), ruleVO.getCompareDesc()};
        return Arrays.stream(charSequences).filter(ObjectUtils::isNotEmpty).collect(Collectors.joining());
    }

    @NotNull
    private static String getValidFormula(ControlRuleVO ruleVO, List<HashMap<String, Object>> errors) {
        StringBuilder validFormula = new StringBuilder();
        if (ruleVO.getSymbol() == null) {
            errors.add(new HashMap<String, Object>() {{
                log.error("公式配置错误!未配置运算符号,ruleId:{}", ruleVO.getId());
                put("公式配置错误!未配置运算符号", ruleVO.getId());
            }});
            return "";
        }
        if (ruleVO.getFormula() == null) {
            errors.add(new HashMap<String, Object>() {{
                log.error("公式配置错误!未配置formula,ruleId:{}", ruleVO.getId());
                put("公式配置错误!未配置formula", ruleVO.getId());
            }});
            return "";
        }
        if (ruleVO.getCompareValue() == null) {
            errors.add(new HashMap<String, Object>() {{
                log.error("公式配置错误!未配置compareValue,ruleId:{}", ruleVO.getId());
                put("公式配置错误!未配置compareValue", ruleVO.getId());
            }});
            return "";
        }
        // 拼接运算左侧
        validFormula.append("(");
        validFormula.append(ruleVO.getFormula());
        validFormula.append(")");
        // 拼接比对符号
        validFormula.append(ruleVO.getSymbol());
        // 拼接运算右侧
        StringBuilder rightFormula = new StringBuilder();
        rightFormula.append("(");
        rightFormula.append(ruleVO.getCompareValue());

        if (ObjectUtils.isNotEmpty(ruleVO.getCompareFormula())) {
            // 包含运算公式
            rightFormula.append("*");
            rightFormula.append("(");
            rightFormula.append(ruleVO.getCompareFormula());
            rightFormula.append(")");
        }

        if (ObjectUtils.isNotEmpty(ruleVO.getCompareType())) {

            if (ruleVO.getCompareType() == 2) {
                // 绝对值
                rightFormula.insert(0, "abs(");
                rightFormula.append(")");
            } else if (ruleVO.getCompareType() == 3) {
                // 百分比
                rightFormula.append("/100");
            }

        }

        rightFormula.append(")");

        validFormula.append(rightFormula);

        log.info("ruleId:{},公式:{}", ruleVO.getId(), validFormula);
        return validFormula.toString();
    }
}
