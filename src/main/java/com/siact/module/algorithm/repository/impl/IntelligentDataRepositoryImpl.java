package com.siact.module.algorithm.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.siact.module.algorithm.entity.IntelligentDataEntity;
import com.siact.module.algorithm.enums.IntelliTypeEnum;
import com.siact.module.algorithm.mapper.IntelligentDataMapper;
import com.siact.module.algorithm.repository.IntelligentDataRepository;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
     * 获取指定类型最后的时间点的智能算法值
     *
     * @param types 要查询的智能算法值类型
     * @return 返回 key 为 dataCode, 值为以类型分组的数据的查询结果
     */
    @Override
    public Map<String, Map<IntelliTypeEnum, IntelligentDataEntity>> queryByTypeWithLastTime(IntelliTypeEnum... types) {
        LambdaQueryWrapper<IntelligentDataEntity> wrapper = Wrappers.<IntelligentDataEntity>lambdaQuery().select(IntelligentDataEntity::getTime).orderByDesc(IntelligentDataEntity::getTime).last("limit 1");
        String time = mapper.selectOne(wrapper).getTime();

        List<IntelligentDataEntity> entities = mapper.selectList(Wrappers.<IntelligentDataEntity>lambdaQuery()
                .in(ArrayUtils.isNotEmpty(types), IntelligentDataEntity::getIntelliType, Arrays.asList(types))
                .eq(IntelligentDataEntity::getTime, time)
        );

        return entities.stream().collect(
                Collectors.groupingBy(IntelligentDataEntity::getDataCode, Collectors.collectingAndThen(
                                Collectors.toList(), list -> list.stream().collect(
                                        Collectors.toMap(IntelligentDataEntity::getIntelliType, o -> o, (v1, v2) -> v2)
                                )
                        )
                )
        );
    }
}
