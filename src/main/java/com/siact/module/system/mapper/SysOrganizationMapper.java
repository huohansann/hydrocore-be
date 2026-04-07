package com.siact.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.system.entity.SysOrganizationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysOrganizationMapper extends BaseMapper<SysOrganizationEntity> {

    @Select("SELECT id, parent_id, org_name, org_code, sort, status FROM sys_organization_new WHERE deleted = 0 ORDER BY sort")
    List<SysOrganizationEntity> queryAllForTree();
}
