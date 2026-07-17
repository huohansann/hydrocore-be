package com.siact.hydrocore.module.base.controller;

import com.github.pagehelper.PageInfo;
import com.siact.hydrocore.common.api.ApiResponse;
import com.siact.hydrocore.module.base.dto.DicDTO;
import com.siact.hydrocore.module.base.dto.DicQuery;
import com.siact.hydrocore.module.base.service.IDicService;
import com.siact.hydrocore.module.base.vo.DicVO;
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
    public ApiResponse<PageInfo<DicVO>> list(DicQuery query) {
        List<DicVO> list = dicService.selectDicList(query);
        return ApiResponse.success(new PageInfo<>(list));
    }

    @ApiOperation("获取字典详细信息")
    @GetMapping("/{id}")
    public ApiResponse<?> getInfo(@PathVariable("id") Long id) {
        return ApiResponse.success(dicService.selectDicById(id));
    }

    @ApiOperation("根据字典类型查询字典数据")
    @GetMapping("/type/{type}")
    public ApiResponse<?> getDicByType(@PathVariable("type") String type) {
        return ApiResponse.success(dicService.selectDicByType(type));
    }

    @ApiOperation("根据字典类型和编码查询字典数据")
    @GetMapping("/type/{type}/code/{code}")
    public ApiResponse<?> getDicByTypeAndCode(@PathVariable("type") String type, @PathVariable("code") String code) {
        return ApiResponse.success(dicService.selectDicByTypeAndCode(type, code));
    }

    @ApiOperation("获取字典树形数据")
    @GetMapping("/tree")
    public ApiResponse<?> tree(DicQuery query) {
        List<DicVO> list = dicService.selectDicList(query);
        return ApiResponse.success(dicService.buildDicTree(list));
    }

    /**
     * 新增字典
     */
    @ApiOperation("新增字典")
    @PostMapping
    public ApiResponse<Void> add(@RequestBody DicDTO dto) {
        return toAjax(dicService.insertDic(dto));
    }

    @ApiOperation("修改字典")
    @PutMapping
    public ApiResponse<Void> edit(@RequestBody DicDTO dto) {
        return toAjax(dicService.updateDic(dto));
    }

    @ApiOperation("删除字典")
    @DeleteMapping("/{ids}")
    public ApiResponse<Void> remove(@PathVariable Long[] ids) {
        return toAjax(dicService.deleteDicByIds(ids));
    }

    /**
     * 响应返回结果
     *
     * @param rows 影响行数
     * @return 操作结果
     */
    private ApiResponse<Void> toAjax(int rows) {
        return rows > 0 ? ApiResponse.success(null) : ApiResponse.fail(500, "操作失败");
    }
}
