package com.siact.module.base.controller;

import com.siact.common.R;
import com.siact.module.base.dto.KilnInfoDTO;
import com.siact.module.base.dto.KilnInfoDistributeDTO;
import com.siact.module.base.dto.KilnInfoGasFlowDTO;
import com.siact.module.base.dto.KilnInfoQuery;
import com.siact.module.base.dto.KilnInfoTotalWindDTO;
import com.siact.module.base.dto.KilnInfoWindDisDTO;
import com.siact.module.base.service.IKilnInfoService;
import com.siact.module.base.vo.KilnInfoVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "炉子基本信息配置")
@RestController
@RequestMapping("/kiln")
public class KilnInfoController {
    @Autowired
    private IKilnInfoService kilnInfoService;

    @ApiOperation("查询炉子信息列表")
    @PostMapping("/list")
    public R list(@RequestBody KilnInfoQuery query) {
        List<KilnInfoVO> list = kilnInfoService.selectKilnInfoList(query);
        return R.data(list);
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

    @ApiOperation("总气量-批量更新")
    @PostMapping("/updateTotalWind")
    public R updateTotalWind(@RequestBody List<KilnInfoTotalWindDTO> list) {
        return toAjax(kilnInfoService.updateTotalWind(list));
    }
} 