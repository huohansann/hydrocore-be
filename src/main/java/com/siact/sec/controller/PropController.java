package com.siact.sec.controller;

import com.siact.api.common.api.vo.prop.NodePropValQueryVo;
import com.siact.api.common.api.vo.prop.PropValFMResultVo;
import com.siact.sec.sevice.PropInsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Slf4j
@RestController
@Api(tags="数字孪生查询属性")
@RequestMapping("/api/prop")
public class PropController {

    @Autowired
    private PropInsService propInsService;

    /**
     * 节点属性历史数据批量查询（动态属性-带公式计算
     */
    @ApiOperation("节点属性历史数据批量查询（动态属性-带公式计算")
    @PostMapping("/queryNodeHistory")
    public List<PropValFMResultVo> queryNodeHistory(@RequestBody NodePropValQueryVo queryVo) {

        return propInsService.queryNodeHistory(queryVo);
    }
}
