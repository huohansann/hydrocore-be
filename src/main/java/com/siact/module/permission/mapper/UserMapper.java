package com.siact.module.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.permission.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 *
 * @author example
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}