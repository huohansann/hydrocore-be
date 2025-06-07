package com.siact.module.control.validator;

import com.alibaba.fastjson.JSONObject;
import com.siact.common.exception.CustomException;
import com.siact.common.utils.JepUtils;
import com.siact.module.base.dto.ControlIntervalConfigDTO;
import com.siact.module.base.dto.GasOperationDTO;
import com.siact.module.base.dto.KilnInfoDistributeDTO;
import com.siact.module.base.dto.TempConditionDTO;
import com.siact.module.base.service.ControlIntervalConfigService;
import com.siact.module.base.service.IRuleService;
import com.siact.module.base.vo.RuleDetailVO;
import com.siact.module.control.dto.RuleFormulaDetailDTO;
import com.siact.sec.sevice.DataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 条规校验
 *
 * @author admin
 */
@Slf4j
@Order(2)
@Component
@ConditionalOnProperty(name = "rule.validator.meta.enable", havingValue = "true", matchIfMissing = true)
public class RuleMetaValidator implements RuleValidator {

    @Autowired
    private IRuleService ruleService;

    @Autowired
    private DataService dataService;

    @Autowired
    private ControlIntervalConfigService controlIntervalService;

    /**
     * 条规校验逻辑
     *
     * @param gasWindSetList 所有炉子的天然气与助燃风设定值
     * @return
     */
    @Override
    public RuleValidateResult validate(List<KilnInfoDistributeDTO> gasWindSetList) {

        // 1:查询所有有效的条规配置
        List<RuleDetailVO> ruleDetailVOs = ruleService.listRules();

        // 2:根据查询到的条规配置 获取组装成公式
        List<RuleFormulaDetailDTO> ruleFormulaDetailList = getRuleFormulaDetailList(ruleDetailVOs);

        // 3:收集所有dataCode
        ArrayList<String> tempConditionsDataCodeList = new ArrayList<>();
        ArrayList<String> gasOperationDataCodeList = new ArrayList<>();
        for (RuleDetailVO detailVO : ruleDetailVOs) {
            // 3.1:查询当前10个点位的数据
            detailVO.getTempConditions().forEach(tempConditionDTO -> tempConditionsDataCodeList.add(tempConditionDTO.getMcCode()));
            // 3.2:查询8个炉子的数据
            detailVO.getGasOperations().forEach(gasOperationDTO -> gasOperationDataCodeList.add(gasOperationDTO.getFurnaceCode()));
        }

        // 4:获取所有参数的当前值  用于jep计算 (包含当前值 和 设定值)
        HashMap<String, BigDecimal> allCalcValMap = getParamValMapForJep(gasWindSetList, tempConditionsDataCodeList, gasOperationDataCodeList);

        // 5:校验拦截 (根据公式进行jep计算,温度和天然气都通过 视为校验通过)
        ArrayList<String> passRuleCodeList = new ArrayList<>();
        for (RuleFormulaDetailDTO ruleFormulaDetailDTO : ruleFormulaDetailList) {
            // 先校验温度限制
            String tempConditionFormula = ruleFormulaDetailDTO.getTempConditionFormula();
            Boolean tempCalcResult = JepUtils.calcBoolean(tempConditionFormula, allCalcValMap, 6, false, null);
            if (Boolean.FALSE.equals(tempCalcResult)) {
                log.error("条规温度校验失败:{}", ruleFormulaDetailDTO.getRuleCode());
                continue;
            }
            // 再校验天然气控制限制
            String gasOperationFormula = ruleFormulaDetailDTO.getGasOperationFormula();
            Boolean gasCalcResult = JepUtils.calcBoolean(gasOperationFormula, allCalcValMap, 6, false, null);
            if (Boolean.FALSE.equals(gasCalcResult)) {
                log.error("条规天然气控制校验失败:{}", ruleFormulaDetailDTO.getRuleCode());
                continue;
            }
            log.info("条规校验通过:{}", ruleFormulaDetailDTO.getRuleCode());
            passRuleCodeList.add(ruleFormulaDetailDTO.getRuleCode());
        }

        // 6:返回校验结果
        // 校验通过的条件有三种 (目前逻辑当所有的rule校验通过 视为通过) TODO 有待确认
        // 1:当所有的rule校验通过 视为通过  2:只要有一条rule校验通过 视为通过 3:全部未通过 则肯定为未通过
        if (passRuleCodeList.size() == ruleDetailVOs.size()) {
            return RuleValidateResult.pass();
        }

        return RuleValidateResult.fail("校验失败!");
    }

    /**
     * 获取所有参数的当前值  用于jep计算 (包含当前值 和 设定值)
     * 逻辑: 分别整理出 当前值的数据格式 和 设定值的数据格式, 然后进行汇总合并
     * @param gasWindSetList
     * @param tempConditionsDataCodeList
     * @param gasOperationDataCodeList
     * @return
     */
    private HashMap<String, BigDecimal> getParamValMapForJep(List<KilnInfoDistributeDTO> gasWindSetList, ArrayList<String> tempConditionsDataCodeList, ArrayList<String> gasOperationDataCodeList) {
        // 查询孪生 获取当前值 (ps:由于温度和天然气设定值  都是查询 瞬时值 ,因此可以同时查询)
        JSONObject dataCodeValObj = querySecLastValByAllDataCode(tempConditionsDataCodeList, gasOperationDataCodeList);

        // 整理当前值  用于 jep计算的当前值
        HashMap<String, BigDecimal> curValMap = getCurValMap(dataCodeValObj);

        // 整理当前的设定值
        HashMap<String, BigDecimal> setValMap = getSetValMap(gasWindSetList, tempConditionsDataCodeList);

        // 通过jep计算获取 boolean值
        HashMap<String, BigDecimal> allCalcValMap = new HashMap<>();
        allCalcValMap.putAll(curValMap);
        allCalcValMap.putAll(setValMap);
        return allCalcValMap;
    }

    /**
     * 将孪生查询到的数据  转化成jep计算格式
     *
     * @param dataCodeValObj
     * @return
     */
    private static HashMap<String, BigDecimal> getCurValMap(JSONObject dataCodeValObj) {
        HashMap<String, BigDecimal> curValMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : dataCodeValObj.entrySet()) {
            String dataCode = entry.getKey();
            Object dataVal = entry.getValue();
            if (ObjectUtils.isEmpty(dataVal)) {
                continue;
            }
            curValMap.put(dataCode + "_curVal", BigDecimal.valueOf(Double.parseDouble(dataVal.toString()))); // 由于 jep计算时  需要区分当前值  因此需要添加_curVal后缀
        }
        return curValMap;
    }

    /**
     * 将设定值 转化为jep计算格式
     *
     * @param gasWindSetList
     * @param tempConditionsDataCodeList
     * @return
     */
    private HashMap<String, BigDecimal> getSetValMap(List<KilnInfoDistributeDTO> gasWindSetList, ArrayList<String> tempConditionsDataCodeList) {
        HashMap<String, BigDecimal> setValMap = new HashMap<>();
        // 1:查询温度设定值
        List<ControlIntervalConfigDTO> temperatureSetList = controlIntervalService.selectListByDataCodeList(tempConditionsDataCodeList);
        Map<String, BigDecimal> temperatureSetMap = temperatureSetList.stream().collect(Collectors.toMap(ControlIntervalConfigDTO::getDataCode, o -> new BigDecimal(o.getTemperatureSet()), (oldValue, newValue) -> newValue));
        setValMap.putAll(temperatureSetMap);
        // 2:查询天然气设定值
        Map<String, BigDecimal> gasSetValMap = gasWindSetList.stream().collect(Collectors.toMap(KilnInfoDistributeDTO::getCode, KilnInfoDistributeDTO::getGasVal, (oldValue, newValue) -> newValue));
        setValMap.putAll(gasSetValMap);
        return setValMap;
    }

    /**
     * 查询孪生当前分钟内温度和天然气的实时值 (最后一包数据)
     *
     * @param tempConditionsDataCodeList
     * @param gasOperationDataCodeList
     * @return
     */
    private JSONObject querySecLastValByAllDataCode(ArrayList<String> tempConditionsDataCodeList, ArrayList<String> gasOperationDataCodeList) {
        ArrayList<String> querySecAllDataCode = new ArrayList<>();
        querySecAllDataCode.addAll(tempConditionsDataCodeList);
        querySecAllDataCode.addAll(gasOperationDataCodeList);
        // 查询最后一包数据
        return dataService.queryRealValue(String.join(",", querySecAllDataCode));
    }

    /**
     * 处理条规的公式
     *
     * @param ruleDetailVOs
     * @return
     */
    private List<RuleFormulaDetailDTO> getRuleFormulaDetailList(List<RuleDetailVO> ruleDetailVOs) {
        List<RuleFormulaDetailDTO> ruleFormulaDetailList = new ArrayList<>();
        // 组装成一个Map 要记录 ruleId MC_公式  gas的公式  然后过滤出mc为true的ruleId  在进行下次计算最终的带过滤出gas为true的ruleId
        ruleDetailVOs.forEach(ruleDetailVO -> {
            List<TempConditionDTO> tempConditions = ruleDetailVO.getTempConditions();
            String tempConditionFormula = buildTempConditionFormulaByList(tempConditions);

            List<GasOperationDTO> gasOperations = ruleDetailVO.getGasOperations();
            String gasOperationFormula = buildGasOperationFormulaByList(gasOperations);

            RuleFormulaDetailDTO ruleFormulaDetailDTO = new RuleFormulaDetailDTO(ruleDetailVO.getId(), ruleDetailVO.getRuleCode(), tempConditionFormula, gasOperationFormula);
            ruleFormulaDetailList.add(ruleFormulaDetailDTO);
        });
        return ruleFormulaDetailList;
    }

    /**
     * 根据ruleId下的多个tempCondition构建公式
     *
     * @param tempConditions
     * @return
     */
    private String buildTempConditionFormulaByList(List<TempConditionDTO> tempConditions) {

        List<String> formulaList = new ArrayList<>();

        tempConditions.forEach(tempCondition -> {
            StringBuilder curFormulaSb = null;

            String mcCode = tempCondition.getMcCode();// 当前温度测点的dataCode
            String operation = tempCondition.getOperation();// 操作符号 ＋ － ±

            String exceedsLimit = tempCondition.getExceedsLimit();// 比较符号 大于 或小于等于
            Double threshold = tempCondition.getThreshold(); // 阈值

            // 当前值要在 设定值范围内
            // 公式拼接逻辑 ( dataCode_curVal + 操作符号 + 阈值 + 比较符号 + dataCode_setVal )
            // 解释: 当前值 加/减 阈值 与 设定值比较  例如: (curVal + 8 <= setVal)

            if ("+".equals(operation) || "-".equals(operation)) {
                // 处理 + 或 - 的单独逻辑
                curFormulaSb = new StringBuilder("(" + mcCode + "_curVal" + operation + threshold + exceedsLimit + mcCode + "_setVal" + ")");
            } else if ("±".equals(operation)) {
                // 处理范围逻辑 TODO
                log.error("暂不支持±操作符号");
            }

            log.info("根据ruleCode:{}下的tempConditionId:{}构建公式:{}", tempCondition.getRuleCode(), tempCondition.getId(), curFormulaSb);
            if (ObjectUtils.isEmpty(curFormulaSb)) {
                log.error("根据ruleCode:{}下的tempConditionId:{}构建公式失败!", tempCondition.getRuleCode(), tempCondition.getId());
                throw new CustomException("根据ruleCode:" + tempCondition.getRuleCode() + "下的tempConditionId:" + tempCondition.getId() + "构建公式失败!");
            }
            formulaList.add(curFormulaSb.toString());
        });
        // 多个温度条件 需要同时满足才算通过 用&&拼接
        return String.join(" && ", formulaList);
    }

    /**
     * 根据ruleId下的多个gasOperation构建公式
     *
     * @param gasOperations
     * @return
     */
    private String buildGasOperationFormulaByList(List<GasOperationDTO> gasOperations) {

        List<String> formulaList = new ArrayList<>();

        gasOperations.forEach(tempCondition -> {
            StringBuilder curFormulaSb = null;

            String furnaceCode = tempCondition.getFurnaceCode();// 需调控炉子的dataCode
            String operation = tempCondition.getOperation();// 操作符号 ＋ － ±

            Double lowVal = tempCondition.getLowVal();// 阈值下限
            Double upVal = tempCondition.getUpVal(); // 阈值上限

            // 设定值要在当前值加/减 阈值上限 和 阈值下限之间
            // 公式拼接逻辑 ( ((dataCode_curVal + 操作符号 + 阈值下限 ) + <= + dataCode_setVal ) && (dataCode_setVal + <= + (dataCode_curVal + 操作符号 + 阈值上限)) )
            // 解释: ( 当前值 加/减 阈值 <= 设定值比较 ) && ( 设定值比较 <= 当前值 加/减 阈值上限 ) 例如: ( (curVal + 8 <= setVal) && (setVal <= curVal + 15) )
            // 注意 当为 减 时  (当前值 - 阈值上限 = 实际的下限)  (当前值 - 阈值的下限 = 实际的上限) 因为减掉的符号是相反的 减得多 实际值更小

            if ("+".equals(operation)) {
                // 处理 + 的单独逻辑
                curFormulaSb = new StringBuilder();
                curFormulaSb.append("(");
                curFormulaSb.append("(" + furnaceCode + "_curVal" + operation + lowVal + "<=" + furnaceCode + "_setVal" + ")");
                curFormulaSb.append(" && ");
                curFormulaSb.append("(" + furnaceCode + "_setVal" + "<=" + furnaceCode + "_curVal" + operation + upVal + ")");
                curFormulaSb.append(")");
            } else if ("-".equals(operation)) {
                // 处理 - 的单独逻辑
                curFormulaSb = new StringBuilder();
                curFormulaSb.append("(");
                curFormulaSb.append("(" + furnaceCode + "_curVal" + operation + upVal + "<=" + furnaceCode + "_setVal" + ")");
                curFormulaSb.append(" && ");
                curFormulaSb.append("(" + furnaceCode + "_setVal" + "<=" + furnaceCode + "_curVal" + operation + lowVal + ")");
                curFormulaSb.append(")");
            } else if ("±".equals(operation)) {
                // 处理范围逻辑 TODO
                log.error("暂不支持±操作符号");
            }

            log.info("根据ruleCode:{}下的gasOperationId:{}构建公式:{}", tempCondition.getRuleCode(), tempCondition.getId(), curFormulaSb);
            if (ObjectUtils.isEmpty(curFormulaSb)) {
                log.error("根据ruleCode:{}下的tempConditionId:{}构建公式失败!", tempCondition.getRuleCode(), tempCondition.getId());
                throw new CustomException("根据ruleCode:" + tempCondition.getRuleCode() + "下的tempConditionId:" + tempCondition.getId() + "构建公式失败!");
            }
            formulaList.add(curFormulaSb.toString());
        });
        // 多个天然气设定条件 需要同时满足才算通过 用&&拼接
        return String.join(" && ", formulaList);
    }
} 