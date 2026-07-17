package com.siact.hydrocore.module.base.controller;

import com.github.pagehelper.PageInfo;
import com.siact.hydrocore.common.R;
import com.siact.hydrocore.module.base.dto.TplDTO;
import com.siact.hydrocore.module.base.dto.TplQuery;
import com.siact.hydrocore.module.base.service.TplService;
import com.siact.hydrocore.module.base.vo.TplVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 模板表 控制器
 *
 * @author siact
 */
@Api(tags = "模板表")
@RestController
@RequestMapping("/tpl")
public class TplController {

    @Autowired
    private TplService tplService;


    @ApiOperation(value = "查询模板列表")
    @GetMapping("/list")
    public R list(TplQuery query) {
        List<TplVO> list = tplService.selectTplList(query);
        return R.data(new PageInfo<>(list));
    }

    @ApiOperation(value = "获取模板详细信息")
    @GetMapping("/{id}")
    public R getInfo(@PathVariable("id") Long id) {
        return R.data(tplService.selectTplById(id));
    }


    @ApiOperation(value = "根据模板类型查询模板数据")
    @GetMapping("/type/{tplType}")
    public R getTplByType(@PathVariable("tplType") String tplType) {
        return R.data(tplService.selectTplByType(tplType));
    }

    @ApiOperation(value = "根据模板编码查询模板数据")
    @GetMapping("/code/{tplCode}")
    public R getTplByCode(@PathVariable("tplCode") String tplCode) {
        return R.data(tplService.selectTplByCode(tplCode));
    }

    @ApiOperation(value = "新增模板")
    @PostMapping
    public R add(@RequestBody TplDTO dto) {
        return toAjax(tplService.insertTpl(dto));
    }

    @ApiOperation(value = "修改模板")
    @PutMapping
    public R edit(@RequestBody TplDTO dto) {
        return toAjax(tplService.updateTpl(dto));
    }

    @ApiOperation(value = "删除模板")
    @DeleteMapping("/{ids}")
    public R remove(@PathVariable Long[] ids) {
        return toAjax(tplService.deleteTplByIds(ids));
    }

    /**
     * 响应返回结果
     *
     * @param rows 影响行数
     * @return 操作结果
     */
    private R toAjax(int rows) {
        return rows > 0 ? R.success() : R.fail("");
    }
} 