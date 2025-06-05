package com.siact.module.control.validator;

import com.siact.module.base.dto.KilnInfoDistributeDTO;

import java.util.List;

public interface RuleValidator {
    RuleValidateResult validate(List<KilnInfoDistributeDTO> list);
} 