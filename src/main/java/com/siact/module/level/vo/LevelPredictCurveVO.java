package com.siact.module.level.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class LevelPredictCurveVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<String> xdata;
    private List<LevelPredictCurveSeriesVO> series;
}
