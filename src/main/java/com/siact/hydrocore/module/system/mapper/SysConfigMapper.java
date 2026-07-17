package com.siact.hydrocore.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.hydrocore.module.system.entity.SysConfigEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统选项配置 Mapper
 *
 * @author siact
 */
@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfigEntity> {
}