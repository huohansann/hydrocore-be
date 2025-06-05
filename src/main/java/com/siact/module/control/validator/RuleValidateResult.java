package com.siact.module.control.validator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 规则校验结果
 *
 * @author wr
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RuleValidateResult {
    private boolean pass;
    private String message;
    // 这里需要根据需求进行拓展字段 todo

    public static RuleValidateResult pass() {
        return new RuleValidateResult(true, "校验通过");
    }

    public static RuleValidateResult fail(String msg) {
        return new RuleValidateResult(false, msg);
    }
} 