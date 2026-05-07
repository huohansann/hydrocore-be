package com.siact.module.control.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.control.dto.ControlRuleDTO;
import com.siact.module.control.dto.ControlRuleQuery;
import com.siact.module.control.entity.ControlRuleEntity;
import com.siact.module.control.vo.ControlRuleVO;

import java.util.List;

public interface ControlRuleService extends IService<ControlRuleEntity> {

    ControlRuleVO selectControlRuleById(Long id);

    List<ControlRuleVO> selectControlRuleList(ControlRuleQuery query);

    List<ControlRuleEntity> queryRuleByTypes(List<Integer> types);

    int insertControlRule(ControlRuleDTO dto);

    int updateControlRule(List<ControlRuleDTO> dtoList);

    int deleteControlRuleByIds(Long[] ids);

    int logicalDeleteControlRuleByIds(Long[] ids);

    /**
     * 判断类型数据是否可以修改
     * @param query
     * @return
     */
    Boolean legal(ControlRuleQuery query);
}
