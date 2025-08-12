package com.siact.module.control.validator;

import com.siact.module.base.entity.KilnInfoEntity;
import com.siact.module.base.service.IKilnInfoService;
import com.siact.module.control.dto.ControlSettingGasDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 总规校验
 *
 * @author admin
 */
@Order(5)
@Component
@Slf4j
public class GeneralRuleValidator implements RuleValidator {
    @Autowired
    private IKilnInfoService kilnInfoService;

    @Override
    public RuleValidateResult validate(List<ControlSettingGasDTO> settingList) {
        // 获取总规信息
        List<KilnInfoEntity> kilnInfoEntities = kilnInfoService.list();

        // 根据id分组
        Map<String, KilnInfoEntity> kilnInfoEntityMap = kilnInfoEntities.stream().collect(Collectors.toMap(KilnInfoEntity::getDataCode,
                vo -> vo, (v1, v2) -> v1));

        List<String> errors = new ArrayList<>();
        for (ControlSettingGasDTO setting : settingList) {
            KilnInfoEntity kilnInfoEntity = kilnInfoEntityMap.get(setting.getDataCode());
            if (kilnInfoEntity == null) {
                // 没有总规就不校验了
                log.warn("总规不存在,number :{}", setting.getNumber());
                continue;
            }
            // 天然气设定值
            BigDecimal gasVal = BigDecimal.valueOf(setting.getGasManualVal());
            BigDecimal gasValLow = kilnInfoEntity.getGasValLow();
            BigDecimal gasValUp = kilnInfoEntity.getGasValUp();
            if (kilnInfoEntity.getGasValLow() == null || kilnInfoEntity.getGasValUp() == null) {
                log.warn("总规没有天然气设定值,number :{}", setting.getNumber());
                continue;
            }

            //  gasValLow  <= gasVal <= gasValUp
            if (gasVal.compareTo(gasValLow) < 0 || gasVal.compareTo(gasValUp) > 0) {
                errors.add(setting.getNumber() + "天然气设定值不在总规范围");
                continue;
            }
        }

        if (CollectionUtils.isNotEmpty(errors)) {
            return RuleValidateResult.fail(errors);
        }
        return RuleValidateResult.pass();
    }

}