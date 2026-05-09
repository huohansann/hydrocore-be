package com.siact.module.predicted.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.siact.common.repository.BaseRepositoryImpl;
import com.siact.module.predicted.entity.PredictedDataEntity;
import com.siact.module.predicted.enums.PredictedTypeEnum;
import com.siact.module.predicted.mapper.PredictedDataMapper;
import com.siact.module.predicted.repository.PredictedDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author : kzuo
 * @version : 1.0
 * @date : 2026-02-25 14:29
 * @className : PredictedDataRepositoryImpl
 * @description : 预测数据数据持久层实现(mybatis 查询代码封装)
 */
@RequiredArgsConstructor
@Repository
public class PredictedDataRepositoryImpl extends BaseRepositoryImpl<PredictedDataMapper, PredictedDataEntity> implements PredictedDataRepository {
    private final PredictedDataMapper mapper;

    /**
     * 根据点位编码, 预测类型, 开始时间~结束时间查询对应的预测数据
     *
     * @param dataCodes 点位编码
     * @param types     预测类型
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 返回按照预测类型分类的 {@link  Map} 集合数据
     */
    @Override
    public Map<Integer, List<PredictedDataEntity>> queryByTypeCode(List<String> dataCodes, List<PredictedTypeEnum> types, String startTime, String endTime) {
        Set<String> typeCodes = types.stream().filter(Objects::nonNull).map(PredictedTypeEnum::getCode).collect(Collectors.toSet());
        List<PredictedDataEntity> predictedDataEntities = mapper.selectList(Wrappers.<PredictedDataEntity>lambdaQuery()
                .in(PredictedDataEntity::getDataCode, dataCodes)
                .in(PredictedDataEntity::getPredictedTypeCode, typeCodes)
                .between(PredictedDataEntity::getTime, startTime, endTime)
                .ge(PredictedDataEntity::getCreateTime, LocalDateTime.now().minusMonths(1))
        );
        return predictedDataEntities.stream().collect(Collectors.groupingBy(PredictedDataEntity::getPredictedType));
    }
}
