package com.siact.module.predicted.repository;

import com.siact.common.repository.BaseRepository;
import com.siact.module.predicted.entity.PredictedDataEntity;
import com.siact.module.predicted.enums.PredictedTypeEnum;

import java.util.List;
import java.util.Map;

/**
 * @author : kzuo
 * @version : 1.0
 * @date : 2026-02-25 14:29
 * @className : PredictedDataRepository
 * @description : 预测数据数据持久层
 */
public interface PredictedDataRepository extends BaseRepository<PredictedDataEntity> {


    /**
     * 根据点位编码, 预测类型, 开始时间~结束时间查询对应的预测数据
     *
     * @param dataCodes 点位编码
     * @param types     预测类型
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 返回按照预测类型分类的 {@link  Map} 集合数据
     */
    Map<Integer, List<PredictedDataEntity>> queryByTypeCode(List<String> dataCodes, List<PredictedTypeEnum> types, String startTime, String endTime);

}
