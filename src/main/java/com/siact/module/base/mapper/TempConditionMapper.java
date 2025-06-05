package com.siact.module.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.base.entity.TempConditionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface TempConditionMapper extends BaseMapper<TempConditionEntity> {
    int insertBatch(@Param("list") List<TempConditionEntity> list);
}