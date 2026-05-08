package com.siact.module.levelcontrol.service;

import com.siact.module.levelcontrol.query.LevelPredictCurveQuery;
import com.siact.module.levelcontrol.vo.LevelPredictCurveVO;
import com.siact.module.levelcontrol.vo.LevelRealtimeVO;

public interface LevelPredictService {
    LevelRealtimeVO getRealtimeData();

    LevelPredictCurveVO queryPredictCurve(LevelPredictCurveQuery query);
}