package com.siact.module.control.validator;

import com.siact.module.base.dto.KilnInfoDistributeDTO;
import com.siact.module.base.entity.KilnInfoEntity;
import com.siact.module.base.service.IKilnInfoService;
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

    @Override
    public RuleValidateResult validate(List<KilnInfoDistributeDTO> list) {
        // 获取总规信息
        List<KilnInfoEntity> kilnInfoEntities = kilnInfoService.list();

        // 根据id分组
        Map<Long, KilnInfoEntity> kilnInfoEntityMap = kilnInfoEntities.stream().collect(Collectors.toMap(KilnInfoEntity::getId,
                vo -> vo, (v1, v2) -> v1));

        List<HashMap<String, Object>> errors = new ArrayList<>();
        for (KilnInfoDistributeDTO distributeDTO : list) {
            KilnInfoEntity kilnInfoEntity = kilnInfoEntityMap.get(distributeDTO.getId());
            if (kilnInfoEntity == null) {
                // 没有总规就不校验了
                log.warn("总规不存在,number :{}", distributeDTO.getNumber());
                continue;
            }
            // 天然气设定值
            BigDecimal gasVal = distributeDTO.getGasVal();
            BigDecimal gasValLow = kilnInfoEntity.getGasValLow();
            BigDecimal gasValUp = kilnInfoEntity.getGasValUp();
            if (kilnInfoEntity.getGasValLow() == null || kilnInfoEntity.getGasValUp() == null) {
                log.warn("总规没有天然气设定值,number :{}", distributeDTO.getNumber());
                continue;
            }

            //  gasValLow  <= gasVal <= gasValUp
            if (gasVal.compareTo(gasValLow) < 0 || gasVal.compareTo(gasValUp) > 0) {
                HashMap<String, Object> errorMap = new HashMap<>();
                errorMap.put(distributeDTO.getNumber(), "天然气设定值不在总规范围");
                errors.add(errorMap);
                continue;
            }
        }

        if (CollectionUtils.isNotEmpty(errors)) {
            return RuleValidateResult.fail(errors);
        }
        return RuleValidateResult.pass();
    }

}