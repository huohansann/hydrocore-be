package com.siact.module.control.validator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    private List<HashMap<String, Object>> errors = new ArrayList<>();

    public RuleValidateResult(boolean pass, String message) {
        this.pass = pass;
        this.message = message;
    }

    public static RuleValidateResult fail(List<HashMap<String, Object>> errors) {
        return new RuleValidateResult(false, "设定值不在范围内").addErrors(errors);
    }

    public RuleValidateResult addError(HashMap<String, Object> error) {
        this.errors.add(error);
        return this;
    }

    public RuleValidateResult addErrors(List<HashMap<String, Object>> errors) {
        this.errors.addAll(errors);
        return this;
    }


    public static RuleValidateResult pass() {
        return new RuleValidateResult(true, "校验通过");
    }

    public static RuleValidateResult fail(String msg) {
        return new RuleValidateResult(false, msg);
    }
} 