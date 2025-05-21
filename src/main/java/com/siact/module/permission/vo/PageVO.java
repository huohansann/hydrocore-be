package com.siact.module.permission.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 分页响应DTO
 *
 * @author example
 * @param <T> 数据类型
 */
@Data
@ApiModel(value = "分页响应结果")
public class PageVO<T> {
    
    @ApiModelProperty(value = "总记录数", example = "100")
    private Long total;
    
    @ApiModelProperty(value = "当前页码", example = "1")
    private Long current;
    
    @ApiModelProperty(value = "每页记录数", example = "10")
    private Long size;
    
    @ApiModelProperty(value = "总页数", example = "10")
    private Long pages;
    
    @ApiModelProperty(value = "数据列表")
    private List<T> records;
    
    /**
     * 构建分页响应
     *
     * @param page MyBatis-Plus分页对象
     * @param <T> 数据类型
     * @return 分页响应对象
     */
    public static <T> PageVO<T> build(IPage<T> page) {
        PageVO<T> response = new PageVO<>();
        response.setTotal(page.getTotal());
        response.setCurrent(page.getCurrent());
        response.setSize(page.getSize());
        response.setPages(page.getPages());
        response.setRecords(page.getRecords());
        return response;
    }
} 