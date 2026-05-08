package com.siact.module.pressure.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: HouBo
 * @Date: 2026/5/8 14:00
 * @Description: 窑压历史数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PressureHistoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String dataCode;
    private List<String> xdata;
    private List<String> ydata;
}
