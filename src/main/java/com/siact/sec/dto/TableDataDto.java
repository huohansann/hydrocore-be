package com.siact.sec.dto;

import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 表格数据前端返回格式
 * @author：GP
 * @date：2024/3/11 15:29
 */
@Data
public class TableDataDto {

    @ApiModelProperty("表头列表")
    List<ExcelExportEntity> headList;

    @ApiModelProperty("数据列表")
    private List<JSONObject> dataList;

    @ApiModelProperty("总条数")
    private Integer total;
}
