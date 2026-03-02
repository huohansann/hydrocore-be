package com.siact.module.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.base.entity.AppConfigEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author : kzuo
 * @version : 1.0
 * @date : 2026-02-27 13:10
 * @className : AppConfigMapper
 * @description : 系统配置数据交互层
 */
@Mapper
public interface AppConfigMapper extends BaseMapper<AppConfigEntity> {
}
