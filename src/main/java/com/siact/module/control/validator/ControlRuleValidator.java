package com.siact.module.control.validator;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.siact.common.constant.ConstantBase;
import com.siact.common.utils.JepUtils;
import com.siact.common.utils.TimeUtil;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(name = "rule.validator.controlRule.enable", havingValue = "true", matchIfMissing = true)
public class ControlRuleValidator implements RuleValidator {

    @Autowired
    private ControlRuleService controlRuleService;

    @Autowired
    private DataService dataService;

    @Override
    public RuleValidateResult validate(List<KilnInfoDistributeDTO> list) {

        List<HashMap<String, Object>> errors = new ArrayList<>();
        // 1: 获取所有的约束规则 (查询所有类型) ps: 已经设置过了换火  液压  炉压 的合法状态
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
        // 结束时间为当前时间
        String endTime = TimeUtil.getNow();
        // 开始时间为当前结束时间减1分钟
        String startTime = TimeUtil.getCalcTime(endTime, 1, ConstantBase.MIN);
        querySecDataParam.setStartTime(startTime);
        querySecDataParam.setEndTime(endTime);
        querySecDataParam.setCalcType(ConstantBase.LAST);

        JSONObject gasSettingDataValJsonObj = dataService.queryBetweenVal(querySecDataParam);

        Map<String, BigDecimal> gasSettingDataValMap =
                ObjectUtils.isEmpty(gasSettingDataValJsonObj) ? new HashMap<>() : com.alibaba.fastjson2.JSONObject.parseObject(gasSettingDataValJsonObj.toJSONString(), new TypeReference<Map<String, BigDecimal>>() {
                });

        if (ObjectUtils.isEmpty(gasSettingDataValMap)) {
            log.error("查询孪生数据查询失败!,未返回点位数据");
            return RuleValidateResult.fail("查询孪生数据失败");
        }

        for (ControlRuleVO ruleVO : ruleVOList) {
            Integer type = ruleVO.getType();
            if (ControlRuleTypeEnum.STEP.getCode().equals(type)) {
                // 校验调节步长
                validateStep(list, ruleVO, errors);
            } else if (ControlRuleTypeEnum.TOTAL_GAS.getCode().equals(type) || ControlRuleTypeEnum.DIFF_GAS.getCode().equals(type)) {
                // 校验总气量 和 气量差值
                validateTotalGasAndDiffGas(ruleVO, errors, gasSettingDataValMap);
            }

        }


        if (CollectionUtils.isNotEmpty(errors)) {
            return RuleValidateResult.fail(errors);
        }
        return RuleValidateResult.pass();
    }

    /**
     * 校验调节步长
     *
     * @param list
     * @param ruleVO
     * @param errors
     */
    private static void validateStep(List<KilnInfoDistributeDTO> list, ControlRuleVO ruleVO, List<HashMap<String, Object>> errors) {
        for (KilnInfoDistributeDTO kilnInfoDistributeDTO : list) {
            // 调节变动值
            HashMap<String, BigDecimal> paramValMap = new HashMap<>();
            paramValMap.put("step", kilnInfoDistributeDTO.getGasValueChange());
            String validFormula = getCalcFormula(ruleVO, errors);
            Boolean result = JepUtils.calcBoolean(validFormula, paramValMap, false, null);
            // 当前结果 与上次结果 相与
            if (result != null) {
                if (!result) {
                    errors.add(new HashMap<String, Object>() {{
                        log.error("{},未通过校验!规则:{}", kilnInfoDistributeDTO.getNumber(), JSON.toJSONString(ruleVO));
                        String errorMsg = buildErrorMsg(ruleVO);
                        put(kilnInfoDistributeDTO.getNumber() + "未通过校验!规则:", errorMsg);
                    }});
                }
                ruleVO.setLegal(ruleVO.getLegal() == null ? result : result && ruleVO.getLegal());
            } else {
                errors.add(new HashMap<String, Object>() {{
                    log.error("{},result结果计算失败!规则:{},公式:{},参数:{}", kilnInfoDistributeDTO.getNumber(), JSON.toJSONString(ruleVO), validFormula, paramValMap);
                    String errorMsg = buildErrorMsg(ruleVO);
                    put(kilnInfoDistributeDTO.getNumber() + "result结果计算失败!规则:", errorMsg);
                }});
            }
        }
    }

    /**
     * 校验总气量 和 气量差值
     *
     * @param ruleVO
     * @param errors
     * @param gasSettingDataValMap
     */
    private static void validateTotalGasAndDiffGas(ControlRuleVO ruleVO, List<HashMap<String, Object>> errors, Map<String, BigDecimal> gasSettingDataValMap) {
        // 校验气量总和 或者 校验气量差 根据公式进行计算
        // 组装校验公式
        String validFormula = getCalcFormula(ruleVO, errors);
        Boolean result = JepUtils.calcBoolean(validFormula, gasSettingDataValMap, false, null);
        if (result != null) {
            ruleVO.setLegal(ruleVO.getLegal() == null ? result : result && ruleVO.getLegal());
        } else {
            errors.add(new HashMap<String, Object>() {{
                log.error("result结果计算失败!规则:{},公式:{},参数:{}", JSON.toJSONString(ruleVO), validFormula, gasSettingDataValMap);
                String errorMsg = buildErrorMsg(ruleVO);
                put("result结果计算失败!规则:", errorMsg);
            }});
        }
        if (!ruleVO.getLegal()) {
            errors.add(new HashMap<String, Object>() {{
                log.error("未通过校验!规则:{}", JSON.toJSONString(ruleVO));
                String errorMsg = buildErrorMsg(ruleVO);
                put("未通过校验!规则:", errorMsg);
            }});
        }
    }

    /**
     * 验证换火、液位、炉压
     * 并将校验状态 设置到 ruleVO 中
     *
     * @param ruleVO
     */
    public static void validateFireAndLiquidAndPressure(ControlRuleVO ruleVO) {
        // 查询 换火 液位 炉压是否异常的状态 TODO 目前点位还没有对接 对接后需要完善逻辑
        Integer type = ruleVO.getType();
        if (ControlRuleTypeEnum.FIRE.getCode().equals(type)) {
            ruleVO.setLegal(true);
        } else if (ControlRuleTypeEnum.LIQUID.getCode().equals(type)) {
            ruleVO.setLegal(true);
        } else if (ControlRuleTypeEnum.PRESSURE.getCode().equals(type)) {
            ruleVO.setLegal(true);
        }
    }


    /**
     * 解析公式当中涉及的dataCode
     * @param formula
     * @return
     */
    private Collection<String> getFormulaDataCode(String formula) {
        // formula 根据运算符号进行分隔
        return Arrays.asList(formula.split("\\+|-|\\*|/"));
    }

    /**
     * 构建错误信息
     * @param ruleVO
     * @return
     */
    @NotNull
    private static String buildErrorMsg(ControlRuleVO ruleVO) {
        CharSequence[] charSequences = {ruleVO.getFormulaDesc() + ruleVO.getSymbol() + ruleVO.getCompareValue().stripTrailingZeros().toPlainString(), ruleVO.getCompareDesc()};
        return Arrays.stream(charSequences).filter(ObjectUtils::isNotEmpty).collect(Collectors.joining());
    }

    /**
     * 根据ruleVo,构建计算公式
     * @param ruleVO
     * @param errors
     * @return
     */
    @NotNull
    private static String getCalcFormula(ControlRuleVO ruleVO, List<HashMap<String, Object>> errors) {
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
        // 1:拼接运算左侧
        validFormula.append("(");
        validFormula.append(ruleVO.getFormula());
        validFormula.append(")");
        // 2:拼接比对符号
        validFormula.append(ruleVO.getSymbol());
        // 3:拼接运算右侧
        StringBuilder rightFormula = new StringBuilder();
        rightFormula.append("(");
        rightFormula.append(ruleVO.getCompareValue());

        if (ObjectUtils.isNotEmpty(ruleVO.getCompareFormula())) {
            // 3.1:处理运算公式
            rightFormula.append("*");
            rightFormula.append("(");
            rightFormula.append(ruleVO.getCompareFormula());
            rightFormula.append(")");
        }
        // 3.2:处理特殊参数类型
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
        // 3.3:处理 单位转换系数
        if (ObjectUtils.isNotEmpty(ruleVO.getFactor())) {
            rightFormula.append("*").append(ruleVO.getFactor());
        }

        rightFormula.append(")");

        validFormula.append(rightFormula);

        log.info("ruleId:{},公式:{}", ruleVO.getId(), validFormula);
        return validFormula.toString();
    }
}
