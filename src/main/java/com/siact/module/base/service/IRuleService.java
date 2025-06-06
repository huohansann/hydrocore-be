package com.siact.module.base.service;

import com.alibaba.fastjson2.JSONObject;
import com.siact.module.base.dto.RuleAddDTO;
import com.siact.module.base.vo.RuleDetailVO;

import java.util.List;

public interface IRuleService {
    /**
     * 查询条规列表
     * @return 条规详情列表
     */
    List<RuleDetailVO> listRules();

    /**
     * 新增条规
     * @param dto 条规数据
     * @return 是否成功
     */
    boolean save(RuleAddDTO dto);

    /**
     * 删除条规
     * @param ruleCode 规则编码
     * @return 是否成功
     */
    boolean delete(String ruleCode);

    /**
     * 条规详情
     * @param ruleCode 规编码
     * @return 条规详情
     */
    RuleDetailVO detail(String ruleCode);

    JSONObject table();
}