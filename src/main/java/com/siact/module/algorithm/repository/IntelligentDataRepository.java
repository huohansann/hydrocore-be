package com.siact.module.algorithm.repository;

import com.siact.module.algorithm.entity.IntelligentDataEntity;
import com.siact.module.algorithm.enums.IntelliTypeEnum;

import java.util.List;
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
     * 获取指定类型在 <code>limit</code> 条件下的智能算法值
     *
     * @param limit 查询限制条件
     * @param types 要查询的智能算法值类型
     * @return 返回 key 为 dataCode, 值为以类型分组的数据的查询结果
     */
    Map<String, Map<IntelliTypeEnum, List<IntelligentDataEntity>>> queryByTypeAndLimit(String limit, IntelliTypeEnum... types);

    /**
     * 获取指定类型最后的时间点的智能算法值
     *
     * @param types 要查询的智能算法值类型
     * @return 返回 key 为 dataCode, 值为以类型分组的数据的查询结果
     */
    Map<String, Map<IntelliTypeEnum, IntelligentDataEntity>> queryByTypeWithLastTime(IntelliTypeEnum... types);

    /**
     * 获取指定类型在指定时间范围内的智能算法值
     *
     * @param dataCodes 点位编码列表
     * @param types      要查询的智能算法值类型
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 返回 key 为 dataCode, 值为以类型分组的数据列表的查询结果
     */
    Map<String, Map<IntelliTypeEnum, List<IntelligentDataEntity>>> queryByTypeAndTimeRange(List<String> dataCodes, List<IntelliTypeEnum> types, String startTime, String endTime);
}
