package com.siact.hydrocore.module.base.controller;

import com.github.pagehelper.PageInfo;
import com.siact.hydrocore.common.api.ApiResponse;
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
    public ApiResponse<PageInfo<TplVO>> list(TplQuery query) {
        List<TplVO> list = tplService.selectTplList(query);
        return ApiResponse.success(new PageInfo<>(list));
    }

    @ApiOperation(value = "获取模板详细信息")
    @GetMapping("/{id}")
    public ApiResponse<?> getInfo(@PathVariable("id") Long id) {
        return ApiResponse.success(tplService.selectTplById(id));
    }


    @ApiOperation(value = "根据模板类型查询模板数据")
    @GetMapping("/type/{tplType}")
    public ApiResponse<?> getTplByType(@PathVariable("tplType") String tplType) {
        return ApiResponse.success(tplService.selectTplByType(tplType));
    }

    @ApiOperation(value = "根据模板编码查询模板数据")
    @GetMapping("/code/{tplCode}")
    public ApiResponse<?> getTplByCode(@PathVariable("tplCode") String tplCode) {
        return ApiResponse.success(tplService.selectTplByCode(tplCode));
    }

    @ApiOperation(value = "新增模板")
    @PostMapping
    public ApiResponse<Void> add(@RequestBody TplDTO dto) {
        return toAjax(tplService.insertTpl(dto));
    }

    @ApiOperation(value = "修改模板")
    @PutMapping
    public ApiResponse<Void> edit(@RequestBody TplDTO dto) {
        return toAjax(tplService.updateTpl(dto));
    }

    @ApiOperation(value = "删除模板")
    @DeleteMapping("/{ids}")
    public ApiResponse<Void> remove(@PathVariable Long[] ids) {
        return toAjax(tplService.deleteTplByIds(ids));
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
