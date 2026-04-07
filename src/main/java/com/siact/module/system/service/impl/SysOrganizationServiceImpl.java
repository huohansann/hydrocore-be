package com.siact.module.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.vo.PageVO;
import com.siact.module.system.command.SysOrganizationCreateCommand;
import com.siact.module.system.command.SysOrganizationUpdateCommand;
import com.siact.module.system.convert.SysOrganizationConvert;
import com.siact.module.system.dto.SysOrganizationQueryDTO;
import com.siact.module.system.entity.SysOrganizationEntity;
import com.siact.module.system.entity.SysUserEntity;
import com.siact.module.system.mapper.SysOrganizationMapper;
import com.siact.module.system.mapper.SysUserMapper;
import com.siact.module.system.query.SysOrganizationQuery;
import com.siact.module.system.repository.SysOrganizationRepository;
import com.siact.module.system.service.SysOrganizationService;
import com.siact.module.system.vo.SysOrganizationTreeVO;
import com.siact.module.system.vo.SysOrganizationVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class SysOrganizationServiceImpl extends ServiceImpl<SysOrganizationMapper, SysOrganizationEntity> implements SysOrganizationService {
    private final SysOrganizationConvert convert;
    private final SysOrganizationRepository repository;
    private final SysUserMapper userMapper;

    @Override
    public PageVO<SysOrganizationVO> list(SysOrganizationQuery query) {
        SysOrganizationQueryDTO queryDTO = convert.toQueryDTO(query);
        Page<SysOrganizationEntity> page = repository.queryList(queryDTO, Page.of(query.getPage(), query.getPageSize()));
        List<SysOrganizationVO> voList = convert.toVOList(page.getRecords());

        return PageVO.<SysOrganizationVO>builder()
                .current(page.getCurrent())
                .size(page.getSize())
                .total(page.getTotal())
                .pages(page.getPages())
                .records(voList)
                .build();
    }

    @Override
    public List<SysOrganizationTreeVO> tree() {
        List<SysOrganizationEntity> allOrgs = repository.queryAllForTree();

        Map<Long, SysOrganizationTreeVO> treeMap = allOrgs.stream()
                .map(convert::toTreeVO)
                .collect(Collectors.toMap(SysOrganizationTreeVO::getId, Function.identity()));

        List<SysOrganizationTreeVO> roots = new ArrayList<>();
        List<SysOrganizationTreeVO> sorted = treeMap.values().stream()
                .sorted(Comparator.comparingLong(SysOrganizationTreeVO::getParentId)
                        .thenComparingInt(SysOrganizationTreeVO::getSort))
                .collect(Collectors.toList());

        for (SysOrganizationTreeVO node : sorted) {
            Long parentId = node.getParentId();
            if (parentId == null || parentId == 0L || !treeMap.containsKey(parentId)) {
                roots.add(node);
            } else {
                SysOrganizationTreeVO parent = treeMap.get(parentId);
                if (CollectionUtils.isEmpty(parent.getChildren())) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(node);
            }
        }
        return roots;
    }

    @Override
    public Boolean create(SysOrganizationCreateCommand command) {
        if (repository.existsByOrgCode(command.getOrgCode())) {
            throw new RuntimeException("组织编码已存在");
        }
        SysOrganizationEntity entity = convert.toEntity(command);
        return this.save(entity);
    }

    @Override
    public Boolean update(SysOrganizationUpdateCommand command) {
        if (repository.existsByOrgCodeExcludeId(command.getOrgCode(), command.getId())) {
            throw new RuntimeException("组织编码已存在");
        }
        SysOrganizationEntity entity = convert.toEntity(command);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        List<SysOrganizationEntity> children = repository.queryByParentId(id);
        if (CollectionUtils.isNotEmpty(children)) {
            throw new RuntimeException("存在子组织，无法删除");
        }
        long userCount = userMapper.selectCount(Wrappers.<SysUserEntity>lambdaQuery()
                .eq(SysUserEntity::getOrgId, id));
        if (userCount > 0) {
            throw new RuntimeException("存在关联用户，无法删除");
        }
        return this.removeById(id);
    }
}
