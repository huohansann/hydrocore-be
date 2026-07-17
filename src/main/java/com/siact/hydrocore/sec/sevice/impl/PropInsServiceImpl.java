package com.siact.hydrocore.sec.sevice.impl;

import com.alibaba.fastjson.JSONObject;
import com.siact.api.common.api.vo.common.R;
import com.siact.api.common.api.vo.prop.NodePropValQueryVo;
import com.siact.api.common.api.vo.prop.PropValFMResultVo;
import com.siact.api.feign.api.ins.PropService;
import com.siact.hydrocore.sec.sevice.PropInsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Slf4j
@Service
public class PropInsServiceImpl implements PropInsService {

    @Autowired
    private PropService propService;

    /**
     * 节点属性历史数据批量查询（动态属性-带公式计算
     * @param queryVo
     * @return
     */
    @Override
     public List<PropValFMResultVo> queryNodeHistory(NodePropValQueryVo queryVo) {
        R<List<PropValFMResultVo>> result = propService.nodeHistory(queryVo);
        if(Objects.equals(result.getCode(), R.OK().getCode())) {
            return result.getData();
        } else {
            log.error("获取目标历史数据失败|{}", JSONObject.toJSONString(result));
            return new ArrayList<>();
        }
    }

}
