package com.siact.module.level.service;

import com.siact.module.level.query.LevelPredictCurveQuery;
import com.siact.module.level.vo.LevelPredictCurveVO;
import com.siact.module.level.vo.LevelRealtimeVO;

public interface LevelPredictService {
    LevelRealtimeVO getRealtimeData();

    LevelPredictCurveVO queryPredictCurve(LevelPredictCurveQuery query);
}