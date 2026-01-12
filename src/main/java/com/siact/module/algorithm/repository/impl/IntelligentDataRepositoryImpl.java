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
     * 获取指定类型最后的时间点的智能算法值
     *
     * @param types 要查询的智能算法值类型
     * @return 返回 key 为 dataCode, 值为以类型分组的数据的查询结果
     */
    @Override
    public Map<String, Map<IntelliTypeEnum, IntelligentDataEntity>> queryByTypeWithLastTime(IntelliTypeEnum... types) {
        Map<String, Map<IntelliTypeEnum, List<IntelligentDataEntity>>> map = this.queryByTypeAndLimit("limit 1, 1", types);
        if (map.isEmpty()) return Collections.emptyMap();

        return map.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey, entry -> entry.getValue().entrySet().stream()
                        .filter(innerEntry -> CollectionUtils.isNotEmpty(innerEntry.getValue()))
                        .collect(Collectors.toMap(Map.Entry::getKey, innerEntry -> innerEntry.getValue().get(0)))
        ));
    }
}
