package com.siact.module.base.controller;

import com.github.pagehelper.PageInfo;
import com.siact.common.R;
import com.siact.module.base.dto.DicDTO;
import com.siact.module.base.dto.DicQuery;
import com.siact.module.base.service.IDicService;
import com.siact.module.base.vo.DicVO;
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
 * 字典表 控制器
 *
 * @author siact
 */
@Api(tags = "字典表")
@RestController
@RequestMapping("/dic")
public class DicController {

    @Autowired
    private IDicService dicService;

    @ApiOperation("查询字典列表")
    @GetMapping("/list")
    public R list(DicQuery query) {
        List<DicVO> list = dicService.selectDicList(query);
        return R.data(new PageInfo<>(list));
    }

    @ApiOperation("获取字典详细信息")
    @GetMapping("/{id}")
    public R getInfo(@PathVariable("id") Long id) {
        return R.data(dicService.selectDicById(id));
    }

    @ApiOperation("根据字典类型查询字典数据")
    @GetMapping("/type/{type}")
    public R getDicByType(@PathVariable("type") String type) {
        return R.data(dicService.selectDicByType(type));
    }

    @ApiOperation("根据字典类型和编码查询字典数据")
    @GetMapping("/type/{type}/code/{code}")
    public R getDicByTypeAndCode(@PathVariable("type") String type, @PathVariable("code") String code) {
        return R.data(dicService.selectDicByTypeAndCode(type, code));
    }

    @ApiOperation("获取字典树形数据")
    @GetMapping("/tree")
    public R tree(DicQuery query) {
        List<DicVO> list = dicService.selectDicList(query);
        return R.data(dicService.buildDicTree(list));
    }

    /**
     * 新增字典
     */
    @ApiOperation("新增字典")
    @PostMapping
    public R add(@RequestBody DicDTO dto) {
        return toAjax(dicService.insertDic(dto));
    }

    @ApiOperation("修改字典")
    @PutMapping
    public R edit(@RequestBody DicDTO dto) {
        return toAjax(dicService.updateDic(dto));
    }

    @ApiOperation("删除字典")
    @DeleteMapping("/{ids}")
    public R remove(@PathVariable Long[] ids) {
        return toAjax(dicService.deleteDicByIds(ids));
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