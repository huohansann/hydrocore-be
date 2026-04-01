package com.siact.module.control.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GasForecastVO implements Serializable {
    private static final long serialVersionUID = 1L;

    List<String> xdata; // 时间轴
    List<GasForecastSeriesVO> series; // 数据集
}
