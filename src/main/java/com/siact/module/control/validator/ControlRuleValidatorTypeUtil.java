package com.siact.module.control.validator;

import com.alibaba.fastjson2.JSON;
import com.siact.common.utils.JepUtils;
import com.siact.module.base.service.TplService;
import com.siact.module.control.dto.ControlRuleLocalTplSettingDTO;
import com.siact.module.control.dto.ControlSettingGasDTO;
import com.siact.module.control.enums.ControlRuleTypeEnum;
import com.siact.module.control.vo.ControlRuleVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ControlRuleValidatorTypeUtil {

    @Autowired
    private TplService tplService;

    /**
     * 校验调节步长
     *
     * @param list
     * @param ruleVO
     * @param errors
     */
    public void validateStep(List<ControlSettingGasDTO> list, ControlRuleVO ruleVO, List<String> errors) {
        for (ControlSettingGasDTO setting : list) {
//            BigDecimal gasValueChange = kilnInfoDistributeDTO.getGasValueChange();
            // 天然气变动值  自动 = 算法计算值 - DCS运行值  人工 = 人工调整值 - DCS运行值
            BigDecimal gasValueChange = BigDecimal.valueOf(setting.getGasManualVal()).subtract(BigDecimal.valueOf(setting.getRunningDcsVal())).abs();// TODO 这段逻辑暂未完善

            if (ObjectUtils.isEmpty(gasValueChange) || gasValueChange.compareTo(BigDecimal.ZERO) == 0) {
                // 变动值为 null 或者 0 视为无变动 可以下发
                log.info("{},气量调节无变动,可以直接下发", setting.getNumber());
                continue;
            }

            // 调节变动值
            HashMap<String, BigDecimal> paramValMap = new HashMap<>();
            paramValMap.put("step", gasValueChange);
            String validFormula = getCalcFormula(ruleVO, errors);
            Boolean result = JepUtils.calcBoolean(validFormula, paramValMap, false, null);
            // 当前结果 与上次结果 相与
            if (result != null) {
                if (!result) {
                    log.error("{},未通过校验!规则:{}", setting.getNumber(), JSON.toJSONString(ruleVO));
                    String errorMsg = buildErrorMsg(ruleVO);
                    errors.add(setting.getNumber() + "未通过校验!规则:" + errorMsg);
                }
                ruleVO.setLegal(ruleVO.getLegal() == null ? result : result && ruleVO.getLegal());
            } else {
                log.error("{},result结果计算失败!规则:{},公式:{},参数:{}", setting.getNumber(), JSON.toJSONString(ruleVO), validFormula, paramValMap);
                String errorMsg = buildErrorMsg(ruleVO);
                errors.add(setting.getNumber() + "result结果计算失败!规则:" + errorMsg);
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
    public void validateTotalGasAndDiffGas(ControlRuleVO ruleVO, List<String> errors, Map<String, BigDecimal> gasSettingDataValMap) {
        // 校验气量总和 或者 校验气量差 根据公式进行计算

        if (ObjectUtils.isEmpty(ruleVO.getCompareValue())) {
            // 当前规则的设定值为 null 跳过约束校验
            log.info("当前规则的设定值为 null 跳过约束校验,rule:{}", JSON.toJSONString(ruleVO));
            return;
        }

        // 组装校验公式
        String validFormula = getCalcFormula(ruleVO, errors);
        Boolean result = JepUtils.calcBoolean(validFormula, gasSettingDataValMap, false, null);
        if (result != null) {
            ruleVO.setLegal(ruleVO.getLegal() == null ? result : result && ruleVO.getLegal());
        } else {
            log.error("result结果计算失败!规则:{},公式:{},参数:{}", JSON.toJSONString(ruleVO), validFormula, gasSettingDataValMap);
            String errorMsg = buildErrorMsg(ruleVO);
            errors.add("result结果计算失败!规则:" + errorMsg);
        }
        if (!ruleVO.getLegal()) {
            log.error("未通过校验!规则:{}", JSON.toJSONString(ruleVO));
            String errorMsg = buildErrorMsg(ruleVO);
            errors.add("未通过校验!规则:" + errorMsg);
        }
    }

    /**
     * 验证换火、液位、炉压
     * 并将校验状态 设置到 ruleVO 中
     *
     * @param ruleVO
     */
    public void validateFireAndLiquidAndPressure(ControlRuleVO ruleVO, List<String> errors) {

        // 读取tpl配置
        ControlRuleLocalTplSettingDTO controlRuleLocalSetting = tplService.getByCode("controlRuleLocalSetting", ControlRuleLocalTplSettingDTO.class);

        // 查询 换火 液位 炉压是否异常的状态 TODO 目前点位还没有对接 对接后需要完善逻辑
        Integer type = ruleVO.getType();
        if (ControlRuleTypeEnum.FIRE.getCode().equals(type)) {
            boolean flag = false;
            if (controlRuleLocalSetting.getFireLocalControl()) {
                flag = controlRuleLocalSetting.getFireLocalStatus();
            } else {
                // TODO 换火状态 实时查询判断逻辑
                flag = true;
            }
            ruleVO.setLegal(flag);
            if (!flag && errors != null) {
                errors.add("当前处于换火期间!无法进行下发");
            }
        } else if (ControlRuleTypeEnum.LIQUID.getCode().equals(type)) {
            boolean flag = false;
            if (controlRuleLocalSetting.getLiquidLocalControl()) {
                flag = controlRuleLocalSetting.getLiquidLocalStatus();
            } else {
                // TODO 液位波动 实时查询判断逻辑
                flag = true;
            }
            ruleVO.setLegal(flag);
            if (!flag && errors != null) {
                errors.add("当前液位波动异常,波动为:" + ruleVO.getCompareValue().stripTrailingZeros().toPlainString() + "!无法进行下发");
            }
        } else if (ControlRuleTypeEnum.PRESSURE.getCode().equals(type)) {
            boolean flag = false;
            if (controlRuleLocalSetting.getPressureLocalControl()) {
                flag = controlRuleLocalSetting.getPressureLocalStatus();
            } else {
                // TODO 液位波动 实时查询判断逻辑
                flag = true;
            }
            ruleVO.setLegal(flag);
            if (!flag && errors != null) {
                errors.add("当前炉压波动异常,波动为:" + ruleVO.getCompareValue().stripTrailingZeros().toPlainString() + "!无法进行下发");
            }
        }
    }

    /**
     * 解析公式当中涉及的dataCode
     *
     * @param formula
     * @return
     */
    private Collection<String> getFormulaDataCode(String formula) {
        // formula 根据运算符号进行分隔
        return Arrays.asList(formula.split("\\+|-|\\*|/"));
    }

    /**
     * 构建错误信息
     *
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
     *
     * @param ruleVO
     * @param errors
     * @return
     */
    @NotNull
    private static String getCalcFormula(ControlRuleVO ruleVO, List<String> errors) {
        StringBuilder validFormula = new StringBuilder();
        if (ruleVO.getSymbol() == null) {
            log.error("公式配置错误!未配置运算符号,ruleId:{}", ruleVO.getId());
            errors.add("公式配置错误!未配置运算符号" + ruleVO.getId());
            return "";
        }
        if (ruleVO.getFormula() == null) {
            log.error("公式配置错误!未配置formula,ruleId:{}", ruleVO.getId());
            errors.add("公式配置错误!未配置formula" + ruleVO.getId());
            return "";
        }
        if (ruleVO.getCompareValue() == null) {
            log.error("公式配置错误!未配置compareValue,ruleId:{}", ruleVO.getId());
            errors.add("公式配置错误!未配置compareValue" + ruleVO.getId());
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
