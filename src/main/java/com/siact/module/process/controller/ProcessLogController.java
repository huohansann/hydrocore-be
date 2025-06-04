package com.siact.module.process.controller;

import com.siact.module.process.dto.ProcessLogDTO;
import com.siact.module.process.service.IProcessLogService;
import com.siact.module.process.vo.ProcessLogVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 工艺日志Controller
 */
@Api(tags = "工艺日志管理")
@RestController
@RequestMapping("/process/log")
public class ProcessLogController {
    @Autowired
    private IProcessLogService processLogService;

    @ApiOperation("分页查询工艺日志")
    @GetMapping("/page")
    public IPage<ProcessLogVO> page(@RequestParam int pageNum, @RequestParam int pageSize, ProcessLogDTO queryDTO) {
        return processLogService.pageQuery(pageNum, pageSize, queryDTO);
    }

    @ApiOperation("查询全部工艺日志")
    @GetMapping("/list")
    public List<ProcessLogVO> list(ProcessLogDTO queryDTO) {
        return processLogService.listAll(queryDTO);
    }

    @ApiOperation("查询单条工艺日志")
    @GetMapping("/{id}")
    public ProcessLogVO get(@PathVariable Long id) {
        return processLogService.getById(id);
    }

    @ApiOperation("新增工艺日志")
    @PostMapping("/add")
    public boolean add(@RequestBody ProcessLogDTO dto) {
        return processLogService.add(dto);
    }

    @ApiOperation("修改工艺日志")
    @PutMapping("/update")
    public boolean update(@RequestBody ProcessLogDTO dto) {
        return processLogService.update(dto);
    }

    @ApiOperation("删除工艺日志")
    @DeleteMapping("/delete/{id}")
    public boolean delete(@PathVariable Long id) {
        return processLogService.delete(id);
    }
} 