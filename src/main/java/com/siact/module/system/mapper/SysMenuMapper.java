package com.siact.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.system.dto.SysMenuQueryDTO;
import com.siact.module.system.entity.SysMenuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-17 9:38
 * @className : SysMenuMapper
 * @description : 系统菜单数据交互层
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenuEntity> {
    List<SysMenuEntity> query(SysMenuQueryDTO queryDTO);

    @Select("select id, code, label, sort, parent_id from sys_menu_new")
    List<SysMenuEntity> queryAllForTree();
}
