package com.siact.module.algorithm.repository;

import com.siact.module.algorithm.entity.IntelligentDataEntity;
import com.siact.module.algorithm.enums.IntelliTypeEnum;

import java.util.Map;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-04 13:50
 * @className : IntelligentDataRepository
 * @description : 智能算法输出数据持久层
 */
public interface IntelligentDataRepository {
    /**
     * 获取指定类型最后的时间点的智能算法值
     *
     * @param types 要查询的智能算法值类型
     * @return 返回 key 为 dataCode, 值为以类型分组的数据的查询结果
     */
    Map<String, Map<IntelliTypeEnum, IntelligentDataEntity>> queryByTypeWithLastTime(IntelliTypeEnum... types);
}
