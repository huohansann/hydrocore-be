package com.siact.module.control.validator;

import com.siact.module.control.dto.ControlSettingGasDTO;

import java.util.List;

public interface RuleValidator {
    RuleValidateResult validate(List<ControlSettingGasDTO> list);
} 