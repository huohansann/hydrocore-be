package com.siact.module.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.vo.PageVO;
import com.siact.module.system.command.SysMenuCreateCommand;
import com.siact.module.system.command.SysMenuUpdateCommand;
import com.siact.module.system.convert.SysMenuConvert;
import com.siact.module.system.dto.SysMenuQueryDTO;
import com.siact.module.system.entity.SysMenuEntity;
import com.siact.module.system.mapper.SysMenuMapper;
import com.siact.module.system.query.SysMenuQuery;
import com.siact.module.system.repository.SysMenuRepository;
import com.siact.module.system.service.SysMenuService;
import com.siact.module.system.vo.SysMenuTreeVO;
import com.siact.module.system.vo.SysMenuVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenuEntity> implements SysMenuService {
    private final SysMenuConvert convert;
    private final SysMenuRepository repository;

    @Override
    public PageVO<SysMenuVO> list(SysMenuQuery query) {
        SysMenuQueryDTO queryDTO = convert.toQueryDTO(query);
        Page<SysMenuEntity> page = repository.queryList(queryDTO, Page.of(query.getPage(), query.getPageSize()));
        List<SysMenuVO> voList = convert.toVOList(page.getRecords());

        return PageVO.<SysMenuVO>builder()
                .current(page.getCurrent())
                .size(page.getSize())
                .total(page.getTotal())
                .pages(page.getPages())
                .records(voList)
                .build();
    }

    @Override
    public List<SysMenuTreeVO> tree() {
        List<SysMenuEntity> allMenus = repository.queryAllForTree();

        Map<Long, SysMenuTreeVO> treeMap = allMenus.stream()
                .map(convert::toTreeVO)
                .collect(Collectors.toMap(SysMenuTreeVO::getId, Function.identity()));

        List<SysMenuTreeVO> roots = new ArrayList<>();
        List<SysMenuTreeVO> sorted = treeMap.values().stream()
                .sorted(Comparator.comparingLong(SysMenuTreeVO::getParentId)
                        .thenComparingInt(SysMenuTreeVO::getSort))
                .collect(Collectors.toList());

        for (SysMenuTreeVO node : sorted) {
            Long parentId = node.getParentId();
            if (parentId == null || parentId == 0L || !treeMap.containsKey(parentId)) {
                roots.add(node);
            } else {
                SysMenuTreeVO parent = treeMap.get(parentId);
                if (CollectionUtils.isEmpty(parent.getChildren())) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(node);
            }
        }
        return roots;
    }

    @Override
    public Boolean create(SysMenuCreateCommand command) {
        SysMenuEntity entity = convert.toEntity(command);
        return this.save(entity);
    }

    @Override
    public Boolean update(SysMenuUpdateCommand command) {
        SysMenuEntity entity = convert.toEntity(command);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        List<SysMenuEntity> children = repository.queryByParentId(id);
        if (CollectionUtils.isNotEmpty(children)) {
            throw new RuntimeException("存在子菜单，无法删除");
        }
        return this.removeById(id);
    }
}
