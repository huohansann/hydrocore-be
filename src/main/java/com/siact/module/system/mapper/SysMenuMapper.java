package com.siact.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.system.entity.SysMenuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenuEntity> {

    @Select("SELECT id, parent_id, menu_name, menu_code, path, icon, sort, type, visible, status FROM sys_menu WHERE deleted = 0 ORDER BY sort")
    List<SysMenuEntity> queryAllForTree();
}
