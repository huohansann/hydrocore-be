package com.siact.module.control.validator;

import com.siact.common.constant.ConstantField;
import com.siact.module.base.dto.ConfigFieldStoreQuery;
import com.siact.module.base.dto.KilnInfoDistributeDTO;
import com.siact.module.base.entity.KilnInfoEntity;
import com.siact.module.base.service.IConfigFieldStoreService;
import com.siact.module.base.service.IKilnInfoService;
import com.siact.module.base.vo.ConfigFieldStoreVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 总规校验
 *
 * @author admin
 */
@Order(1)
@Component
@Slf4j
public class GeneralRuleValidator implements RuleValidator {
    @Autowired
    private IKilnInfoService kilnInfoService;

    @Autowired
    private IConfigFieldStoreService configFieldStoreService;


    @Override
    public RuleValidateResult validate(List<KilnInfoDistributeDTO> list) {
        // 获取总规信息
        List<KilnInfoEntity> kilnInfoEntities = kilnInfoService.list();

        // 根据id分组
        Map<Long, KilnInfoEntity> kilnInfoEntityMap = kilnInfoEntities.stream().collect(Collectors.toMap(KilnInfoEntity::getId,
                vo -> vo, (v1, v2) -> v1));

        BigDecimal totalGasVal1To5 = BigDecimal.ZERO;
        BigDecimal totalGasVal6To8 = BigDecimal.ZERO;
        List<HashMap<String, Object>> errors = new ArrayList<>();
        for (KilnInfoDistributeDTO distributeDTO : list) {
            KilnInfoEntity kilnInfoEntity = kilnInfoEntityMap.get(distributeDTO.getId());
            String number = distributeDTO.getNumber();
            if (kilnInfoEntity == null) {
                // 没有总规就不校验了
                log.warn("总规不存在,number :{}", distributeDTO.getNumber());
                continue;
            }
            // 天然气设定值
            BigDecimal gasVal = distributeDTO.getGasVal();
            BigDecimal gasValLow = kilnInfoEntity.getGasValLow();
            BigDecimal gasValUp = kilnInfoEntity.getGasValUp();
            //  gasValLow  <= gasVal <= gasValUp
            if (gasVal.compareTo(gasValLow) < 0 || gasVal.compareTo(gasValUp) > 0) {
                HashMap<String, Object> errorMap = new HashMap<>();
                errorMap.put(distributeDTO.getNumber(), "天然气设定值不在总规范围");
                errors.add(errorMap);
                continue;
            }
            int num = Integer.parseInt(number.split("#")[0]);
            if (num <= 5 && num >= 1) {
                totalGasVal1To5 = totalGasVal1To5.add(gasVal);
            }
            if (num >= 6 && num <= 8) {
                totalGasVal6To8 = totalGasVal6To8.add(gasVal);
            }
        }

        // 校验总气量
        validTotalGasVal(totalGasVal1To5, errors, totalGasVal6To8);

        if (CollectionUtils.isNotEmpty(errors)) {
            return RuleValidateResult.fail(errors);
        }
        return RuleValidateResult.pass();
    }

    private void validTotalGasVal(BigDecimal totalGasVal1To5, List<HashMap<String, Object>> errors,
                                  BigDecimal totalGasVal6To8) {
        ConfigFieldStoreQuery storeQuery = new ConfigFieldStoreQuery();
        storeQuery.setFieldKey(ConstantField.TOTAL_GAS_VOLUME);
        storeQuery.setIsLike(true);
        List<ConfigFieldStoreVO> configFieldStoreVOList =
                configFieldStoreService.selectConfigFieldStoreList(storeQuery);

        if (CollectionUtils.isEmpty(configFieldStoreVOList)) {
            log.warn("总气量和未配置");
            return;
        }

        Map<String, BigDecimal> totalGasValMap = configFieldStoreVOList.stream()
                .collect(Collectors.toMap(
                        ConfigFieldStoreVO::getFieldKey,
                        vo -> vo.getFieldValue() != null ? new BigDecimal(vo.getFieldValue()) : null,
                        (v1, v2) -> v1
                ));

        checkAndAddError(errors, ConstantField.TOTAL_GAS_VOLUME_1_5, "1-5气量总和", totalGasVal1To5, totalGasValMap);
        checkAndAddError(errors, ConstantField.TOTAL_GAS_VOLUME_6_8, "6-8气量总和", totalGasVal6To8, totalGasValMap);
    }

    private void checkAndAddError(List<HashMap<String, Object>> errors,
                                  String fieldKey,
                                  String errorKey,
                                  BigDecimal actualValue,
                                  Map<String, BigDecimal> totalGasValMap) {
        BigDecimal configuredValue = totalGasValMap.get(fieldKey);
        if (configuredValue == null) {
            log.warn("{}未配置", errorKey);
            return;
        }

        if (actualValue != null && configuredValue.compareTo(actualValue) >= 0) {
            HashMap<String, Object> errorMap = new HashMap<>();
            errorMap.put(errorKey, "不能低于配置值:" + configuredValue);
            errors.add(errorMap);
        }
    }

} 