package com.siact.module.snapshot.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 快照视图对象
 *
 * @author Roo
 * @date 2025-09-22
 */
@Data
public class SnapshotChartVO {

    @ApiModelProperty("图表数据")
    private List<SnapshotChartDetailVO> chartData;

    @ApiModelProperty(value = "x轴数据")
    private List<String> xAxis;

}