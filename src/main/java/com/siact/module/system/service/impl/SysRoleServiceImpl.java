package com.siact.module.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.vo.PageVO;
import com.siact.module.system.command.SysRoleCreateCommand;
import com.siact.module.system.command.SysRoleUpdateCommand;
import com.siact.module.system.convert.SysRoleConvert;
import com.siact.module.system.dto.SysRoleQueryDTO;
import com.siact.module.system.entity.SysRoleEntity;
import com.siact.module.system.mapper.SysRoleMapper;
import com.siact.module.system.mapper.SysRoleMenuMapper;
import com.siact.module.system.query.SysRoleQuery;
import com.siact.module.system.repository.SysRoleRepository;
import com.siact.module.system.service.SysRoleService;
import com.siact.module.system.vo.SysRoleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRoleEntity> implements SysRoleService {
    private final SysRoleConvert convert;
    private final SysRoleRepository repository;
    private final SysRoleMenuMapper roleMenuMapper;

    @Override
    public PageVO<SysRoleVO> list(SysRoleQuery query) {
        SysRoleQueryDTO queryDTO = convert.toQueryDTO(query);
        Page<SysRoleEntity> page = repository.queryList(queryDTO, Page.of(query.getPage(), query.getPageSize()));
        List<SysRoleVO> voList = convert.toVOList(page.getRecords());

        return PageVO.<SysRoleVO>builder()
                .current(page.getCurrent())
                .size(page.getSize())
                .total(page.getTotal())
                .pages(page.getPages())
                .records(voList)
                .build();
    }

    @Override
    public Boolean create(SysRoleCreateCommand command) {
        if (repository.existsByRoleCode(command.getRoleCode())) {
            throw new RuntimeException("角色编码已存在");
        }
        SysRoleEntity entity = convert.toEntity(command);
        return this.save(entity);
    }

    @Override
    public Boolean update(SysRoleUpdateCommand command) {
        if (repository.existsByRoleCodeExcludeId(command.getRoleCode(), command.getId())) {
            throw new RuntimeException("角色编码已存在");
        }
        SysRoleEntity entity = convert.toEntity(command);
        return this.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        roleMenuMapper.deleteByRoleId(id);
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.deleteByRoleId(roleId);
        if (menuIds != null && !menuIds.isEmpty()) {
            roleMenuMapper.batchInsert(roleId, menuIds);
        }
    }

    @Override
    public List<Long> getMenuIds(Long roleId) {
        return roleMenuMapper.selectMenuIdsByRoleId(roleId);
    }
}
