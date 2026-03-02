package com.siact.module.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.vo.PageVO;
import com.siact.module.system.command.SysMenuCreateCommand;
import com.siact.module.system.command.SysMenuDeleteCommand;
import com.siact.module.system.convert.SysMenuConvert;
import com.siact.module.system.dto.SysMenuQueryDTO;
import com.siact.module.system.entity.SysMenuEntity;
import com.siact.module.system.enums.MenuDeleteType;
import com.siact.module.system.mapper.SysMenuMapper;
import com.siact.module.system.query.SysMenuQuery;
import com.siact.module.system.repository.SysMenuRepository;
import com.siact.module.system.service.SysMenuService;
import com.siact.module.system.vo.SysMenuTreeVO;
import com.siact.module.system.vo.SysMenuVO;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-17 9:37
 * @className : SysMenuServiceImpl
 * @description : 系统菜单业务类实现
 */
@RequiredArgsConstructor
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenuEntity> implements SysMenuService {
    private final SysMenuConvert convert;
    private final SysMenuRepository repository;
    private final SysMenuMapper mapper;

    @Override
    public PageVO<SysMenuVO> list(SysMenuQuery query) {
        SysMenuQueryDTO queryDTO = convert.toQueryDTO(query);

        Page<SysMenuEntity> page = repository.queryList(queryDTO, Page.of(query.getPage(), query.getPageSize()));
        List<SysMenuVO> menuVOList = convert.toVO(page.getRecords());

        return PageVO.<SysMenuVO>builder()
                .current(page.getCurrent())
                .size(page.getSize())
                .total(page.getTotal())
                .pages(page.getPages())
                .records(menuVOList)
                .build();
    }

    @Override
    public List<SysMenuTreeVO> tree() {
        List<SysMenuEntity> menuEntities = mapper.queryAllForTree();

        // 按照 parentId 分组得到所有菜单组
        // Map<Long, List<SysMenuEntity>> childrenGroup = menuEntities.stream().filter(menu -> Objects.nonNull(menu.getParentId())).collect(Collectors.groupingBy(SysMenuEntity::getParentId));
        // 获取所有父级菜单 id
        // Set<Long> parentMenuIds = childrenGroup.keySet();
        // 保留有子菜单的菜单项
        // List<SysMenuEntity> parentMenus = menuEntities.stream().filter(menu -> parentMenuIds.contains(menu.getId()) || menu.getParentId() == 0L).collect(Collectors.toList());
        // 对象转换分组
        Map<Long, SysMenuTreeVO> treeMap = menuEntities.stream().map(convert::toTreeVO).collect(Collectors.toMap(SysMenuTreeVO::getId, Function.identity()));

        // 组装树结构
        List<SysMenuTreeVO> roots = new ArrayList<>();
        List<SysMenuTreeVO> collect = treeMap.values().stream().sorted(Comparator.comparingLong(SysMenuTreeVO::getParentId).thenComparingInt(SysMenuTreeVO::getSort)).collect(Collectors.toList());
        for (SysMenuTreeVO node : collect) {
            Long parentId = node.getParentId();
            if (Objects.isNull(parentId) || !treeMap.containsKey(parentId)) {
                // 顶级父菜单
                roots.add(node);
            } else {
                SysMenuTreeVO parent = treeMap.get(parentId);
                if (CollectionUtils.isEmpty(parent.getChildren())) parent.setChildren(new ArrayList<>());
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
    public Boolean delete(List<SysMenuDeleteCommand> commands) {
        Map<MenuDeleteType, List<String>> delMap = commands.stream().filter(v -> StringUtils.isNotBlank(v.getValue()))
                .collect(Collectors.groupingBy(SysMenuDeleteCommand::getType, Collectors.collectingAndThen(
                        Collectors.toList(), list -> list.stream().map(SysMenuDeleteCommand::getValue).collect(Collectors.toList())
                )));

        return repository.delete(delMap);
    }
}
