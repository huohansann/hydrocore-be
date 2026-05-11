package com.siact.module.algorithm.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.siact.module.algorithm.entity.IntelligentDataEntity;
import com.siact.module.algorithm.enums.IntelliTypeEnum;
import com.siact.module.algorithm.mapper.IntelligentDataMapper;
import com.siact.module.algorithm.repository.IntelligentDataRepository;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-04 13:52
 * @className : IntelligentDataRepositoryImpl
 * @description : 智能算法输出数据持久层实现
 */
@AllArgsConstructor
@Repository
public class IntelligentDataRepositoryImpl implements IntelligentDataRepository {
    private final IntelligentDataMapper mapper;

    /**
     * 获取指定类型在 <code>limit</code> 条件下的智能算法值
     *
     * @param limit 查询限制条件
     * @param types 要查询的智能算法值类型
     * @return 返回 key 为 dataCode, 值为以类型分组的数据的查询结果
     */
    @Override
    public Map<String, Map<IntelliTypeEnum, List<IntelligentDataEntity>>> queryByTypeAndLimit(String limit, IntelliTypeEnum... types) {
        if (StringUtils.isBlank(limit) || ArrayUtils.isEmpty(types)) return Collections.emptyMap();
        LambdaQueryWrapper<IntelligentDataEntity> wrapper = Wrappers.<IntelligentDataEntity>lambdaQuery().select(IntelligentDataEntity::getTime).orderByDesc(IntelligentDataEntity::getTime).last(limit);
        String time = Optional.ofNullable(mapper.selectOne(wrapper)).orElse(new IntelligentDataEntity()).getTime();
        if (StringUtils.isBlank(time)) return Collections.emptyMap();

        List<IntelligentDataEntity> entities = mapper.selectList(Wrappers.<IntelligentDataEntity>lambdaQuery()
                .in(IntelligentDataEntity::getIntelliType, Arrays.asList(types))
                .eq(IntelligentDataEntity::getTime, time)
        );

        return entities.stream().collect(
                Collectors.groupingBy(IntelligentDataEntity::getDataCode, Collectors.collectingAndThen(
                        Collectors.toList(), list -> list.stream().collect(Collectors.groupingBy(IntelligentDataEntity::getIntelliType))
                ))
        );
    }

    /**
     * @param types 要查询的智能算法值类型
     * @return 返回 key 为 dataCode, 值为以类型分组的数据的查询结果
     * @update: 查询最新的 rule_valid = 1 的数据，同时返回最新数据的校验状态
     */
    @Override
    public Map<String, Map<IntelliTypeEnum, IntelligentDataEntity>> queryByTypeWithLastTime(IntelliTypeEnum... types) {
        // 1. 查询最新一条 rule_valid = 1 的记录，获取时间点
        IntelligentDataEntity latestValidEntity = mapper.selectOne(Wrappers.<IntelligentDataEntity>lambdaQuery()
                .in(IntelligentDataEntity::getIntelliType, Arrays.asList(types))
                .eq(IntelligentDataEntity::getRuleValid, true)
                .orderByDesc(IntelligentDataEntity::getTime)
                .last("limit 1"));
        if (latestValidEntity == null) return Collections.emptyMap();
        String validTime = latestValidEntity.getTime();

        // 2. 查询该时间点所有 rule_valid = 1 的数据
        List<IntelligentDataEntity> entities = mapper.selectList(Wrappers.<IntelligentDataEntity>lambdaQuery()
                .in(IntelligentDataEntity::getIntelliType, Arrays.asList(types))
                .eq(IntelligentDataEntity::getTime, validTime)
                .eq(IntelligentDataEntity::getRuleValid, true));

        // 3. 查询绝对最新记录的 rule_valid 状态
        IntelligentDataEntity absoluteLatest = mapper.selectOne(Wrappers.<IntelligentDataEntity>lambdaQuery()
                .in(IntelligentDataEntity::getIntelliType, Arrays.asList(types))
                .orderByDesc(IntelligentDataEntity::getTime)
                .last("limit 1"));
        boolean latestValid = absoluteLatest != null && Boolean.TRUE.equals(absoluteLatest.getRuleValid());

        // 4. 组装结果，根据最新记录设置 ruleValid
        boolean finalLatestValid = latestValid;
        return entities.stream().collect(
                Collectors.groupingBy(IntelligentDataEntity::getDataCode,
                        Collectors.collectingAndThen(Collectors.toList(), list ->
                                list.stream().collect(Collectors.toMap(
                                        IntelligentDataEntity::getIntelliType,
                                        entity -> { entity.setRuleValid(finalLatestValid); return entity; }
                                ))
                        ))
        );
    }

    /**
     * 获取指定类型在指定时间范围内的智能算法值
     *
     * @param dataCodes 点位编码列表
     * @param types      要查询的智能算法值类型
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 返回 key 为 dataCode, 值为以类型分组的数据列表的查询结果
     */
    @Override
    public Map<String, Map<IntelliTypeEnum, List<IntelligentDataEntity>>> queryByTypeAndTimeRange(List<String> dataCodes, List<IntelliTypeEnum> types, String startTime, String endTime) {
        if (CollectionUtils.isEmpty(dataCodes) || CollectionUtils.isEmpty(types) || StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
            return Collections.emptyMap();
        }
        List<IntelligentDataEntity> entities = mapper.selectList(Wrappers.<IntelligentDataEntity>lambdaQuery()
                .in(IntelligentDataEntity::getDataCode, dataCodes)
                .in(IntelligentDataEntity::getIntelliType, types)
                .ge(IntelligentDataEntity::getTime, startTime)
                .le(IntelligentDataEntity::getTime, endTime)
                .orderByAsc(IntelligentDataEntity::getTime)
        );

        return entities.stream().collect(
                Collectors.groupingBy(IntelligentDataEntity::getDataCode, Collectors.collectingAndThen(
                        Collectors.toList(), list -> list.stream().collect(Collectors.groupingBy(IntelligentDataEntity::getIntelliType))
                ))
        );
    }
}
