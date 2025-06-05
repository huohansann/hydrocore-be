package com.siact.module.control.validator;

import com.siact.module.base.dto.KilnInfoDistributeDTO;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 条规校验
 *
 * @author admin
 */
@Order(2)
@Component
public class RuleMetaValidator implements RuleValidator {
    @Override
    public RuleValidateResult validate(List<KilnInfoDistributeDTO> list) {
        // TODO: 实现条规校验逻辑
        return RuleValidateResult.pass();
    }
} 