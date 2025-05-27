package com.siact.module.predicted.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.predicted.entity.PredictedDataEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PredictedDataMapper extends BaseMapper<PredictedDataEntity> {
    List<PredictedDataEntity> getPredictedDataByTypes(
            @Param("dataCodeList") List<String> dataCodeList, @Param("predictedTypeList") List<Integer> predictedTypeList,
            @Param("startTime") String startTime, @Param("endTime") String endTime);
}
