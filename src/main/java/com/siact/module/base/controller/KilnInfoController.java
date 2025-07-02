package com.siact.module.base.controller;

import com.alibaba.fastjson2.JSONObject;
import com.siact.common.R;
import com.siact.module.base.dto.*;
import com.siact.module.base.service.IKilnInfoService;
import com.siact.module.base.vo.KilnInfoVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Api(tags = "炉子基本信息配置")
@RestController
@RequestMapping("/kiln")
public class KilnInfoController {
    @Autowired
    private IKilnInfoService kilnInfoService;

    @ApiOperation("查询炉子信息列表")
    @PostMapping("/list")
    public R<List<JSONObject>> list(@RequestBody KilnInfoQuery query) {
        List<KilnInfoVO> list = kilnInfoService.selectKilnInfoList(query);

        List<JSONObject> rtnData = new ArrayList<>();
        for (KilnInfoVO kilnInfoVO : list) {
            JSONObject curObj = new JSONObject();
            curObj.put("id", kilnInfoVO.getId());
            curObj.put("number", kilnInfoVO.getNumber());
            curObj.put("code", kilnInfoVO.getCode());
            curObj.put("state", kilnInfoVO.getState());
            curObj.put("gasCalc", kilnInfoVO.getGasCalc() == null ? null : kilnInfoVO.getGasCalc().doubleValue());
            curObj.put("gasVal", kilnInfoVO.getGasVal() == null ? null : kilnInfoVO.getGasVal().doubleValue());
            // 天然气变动值 = 当前的设定值 - 上一个的设定值
            curObj.put("gasValueChange", kilnInfoVO.getGasValueChange() == null ? null : kilnInfoVO.getGasValueChange().doubleValue());
            curObj.put("windVal", kilnInfoVO.getWindVal() == null ? null : kilnInfoVO.getWindVal().doubleValue());
            curObj.put("windGasRate", kilnInfoVO.getWindGasRate() == null ? null : kilnInfoVO.getWindGasRate().doubleValue());
            curObj.put("gasValUp", kilnInfoVO.getGasValUp() == null ? null : kilnInfoVO.getGasValUp().doubleValue());
            curObj.put("gasValLow", kilnInfoVO.getGasValLow() == null ? null : kilnInfoVO.getGasValLow().doubleValue());
            curObj.put("windDisUp", kilnInfoVO.getWindDisUp() == null ? null : kilnInfoVO.getWindDisUp().doubleValue());
            curObj.put("windDisLow", kilnInfoVO.getWindDisLow() == null ? null : kilnInfoVO.getWindDisLow().doubleValue());
            rtnData.add(curObj);
        }

        return R.data(rtnData);
    }

    @ApiOperation("获取炉子详细信息")
    @GetMapping("/get/{id}")
    public R getInfo(@PathVariable("id") Long id) {
        return R.data(kilnInfoService.selectKilnInfoById(id));
    }

    @ApiOperation("新增炉子信息")
    @PostMapping("/add")
    public R add(@RequestBody KilnInfoDTO dto) {
        return toAjax(kilnInfoService.insertKilnInfo(dto));
    }


    @ApiOperation("修改炉子信息")
    @PostMapping("/edit")
    public R edit(@RequestBody KilnInfoDTO dto) {
        return toAjax(kilnInfoService.updateKilnInfo(dto));
    }

    @ApiOperation("删除炉子信息")
    @GetMapping("/delete/{ids}")
    public R delete(@PathVariable Long[] ids) {
        return toAjax(kilnInfoService.deleteKilnInfoByIds(ids));
    }

    private R toAjax(int rows) {
        return rows > 0 ? R.success() : R.fail("");
    }


    @ApiOperation("批量保存炉子信息")
    @PostMapping("/saveOrUpdateBatch")
    public R saveOrUpdateBatch(@RequestBody List<KilnInfoDTO> list) {
        return toAjax(kilnInfoService.saveKilnInfoBatch(list));
    }

    @ApiOperation("天然气与风气值下发-批量更新")
    @PostMapping("/updateDistribute")
    public R updateDistribute(@RequestBody List<KilnInfoDistributeDTO> list) {
        return toAjax(kilnInfoService.updateDistribute(list));
    }


    @ApiOperation("天然气流量设定值上下限-批量更新")
    @PostMapping("/updateGasFlow")
    public R updateGasFlow(@RequestBody List<KilnInfoGasFlowDTO> list) {
        return toAjax(kilnInfoService.updateGasFlow(list));
    }

    @ApiOperation("气量分布-批量更新")
    @PostMapping("/updateWindDis")
    public R updateWindDis(@RequestBody List<KilnInfoWindDisDTO> list) {
        return toAjax(kilnInfoService.updateWindDis(list));
    }
} 