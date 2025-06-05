package com.siact.module.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.base.entity.GasOperationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface GasOperationMapper extends BaseMapper<GasOperationEntity> {
    int insertBatch(@Param("list") List<GasOperationEntity> list);
}