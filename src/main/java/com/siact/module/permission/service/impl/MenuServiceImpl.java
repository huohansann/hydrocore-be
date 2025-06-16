package com.siact.module.permission.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.utils.ConvertUtils;
import com.siact.module.permission.dto.MenuDTO;
import com.siact.module.permission.dto.PageDTO;
import com.siact.module.permission.entity.MenuEntity;
import com.siact.module.permission.mapper.MenuMapper;
import com.siact.module.permission.service.MenuService;
import com.siact.module.permission.vo.MenuVO;
import com.siact.module.permission.vo.PageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单Service实现类
 *
 * @author example
 */
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, MenuEntity> implements MenuService {


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveMenu(MenuDTO request) {
        MenuEntity menu = new MenuEntity();
        BeanUtil.copyProperties(request, menu);

        // 默认值设置
        if (menu.getModelShow() == null) {
            menu.setModelShow(true);
        }
        if (menu.getStatus() == null) {
            menu.setStatus(true);
        }
        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }

        // 如果code为空，则按照名称自动生成
        if (StrUtil.isBlank(menu.getMenuCode())) {
            String menueCode = PinyinUtil.getFirstLetter(menu.getMenuName(),"").trim().toUpperCase();
            menu.setMenuCode(menueCode);
        }

        return save(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMenu(MenuDTO request) {
        if (request.getId() == null) {
            return false;
        }

        MenuEntity menu = getById(request.getId());
        if (menu == null) {
            return false;
        }

        // 禁止将自己设为自己的父级
        if (request.getParentId() != null && request.getParentId().equals(request.getId())) {
            return false;
        }

        // 禁止将自己设为自己的子级的父级
        if (request.getParentId() != null && request.getParentId() > 0) {
            // 获取所有子菜单ID
            List<Long> childIds = getChildIds(request.getId());
            if (childIds.contains(request.getParentId())) {
                return false;
            }
        }

        BeanUtil.copyProperties(request, menu);

        return updateById(menu);
    }

    /**
     * 获取所有子菜单ID
     *
     * @param id 菜单ID
     * @return 子菜单ID列表
     */
    private List<Long> getChildIds(Long id) {
        List<Long> childIds = new ArrayList<>();
        List<MenuEntity> children = list(new LambdaQueryWrapper<MenuEntity>()
                .eq(MenuEntity::getParentId, id));

        if (CollUtil.isNotEmpty(children)) {
            // 添加直接子菜单ID
            List<Long> directChildIds = children.stream()
                    .map(MenuEntity::getId)
                    .collect(Collectors.toList());
            childIds.addAll(directChildIds);

            // 递归添加间接子菜单ID
            for (Long childId : directChildIds) {
                childIds.addAll(getChildIds(childId));
            }
        }

        return childIds;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMenu(Long id) {
        // 检查是否有子菜单
        long count = count(new LambdaQueryWrapper<MenuEntity>()
                .eq(MenuEntity::getParentId, id));
        if (count > 0) {
            return false;
        }

        return removeById(id);
    }

    @Override
    public List<MenuVO> getMenuTree(Long parentId, Integer modelShow) {
        LambdaQueryWrapper<MenuEntity> menuLambdaQueryWrapper = new LambdaQueryWrapper<MenuEntity>()
                .eq(modelShow != null, MenuEntity::getModelShow, modelShow)
                .orderByAsc(MenuEntity::getId);
        List<MenuEntity> allMenus = list(menuLambdaQueryWrapper);
        // 将列表转为树形结构
        return buildTree(allMenus, parentId);
    }

    /**
     * 构建菜单树
     *
     * @param allMenus 所有菜单列表
     * @return 树形结构菜单列表
     */
    private List<MenuVO> buildTree(List<MenuEntity> allMenus, Long parentId) {
        if (CollUtil.isEmpty(allMenus)) {
            return new ArrayList<>();
        }

        // 类型装换
        List<MenuVO> menuVOS = ConvertUtils.sourceToTarget(allMenus, MenuVO.class);

        // 按父ID分组
        Map<Long, List<MenuVO>> parentIdMap = menuVOS.stream().collect(Collectors.groupingBy(MenuVO::getParentId));

        // 获取顶级菜单
        List<MenuVO> rootMenus = parentId != null ? parentIdMap.getOrDefault(parentId, new ArrayList<>()) : menuVOS.stream().filter(menu -> 0 == menu.getType()).collect(Collectors.toList());

        // 递归设置子菜单
        rootMenus.forEach(menu -> setChildren(menu, parentIdMap));

        return rootMenus;
    }

    /**
     * 设置子菜单
     *
     * @param menu        当前菜单
     * @param parentIdMap 按父ID分组的菜单Map
     */
    private void setChildren(MenuVO menu, Map<Long, List<MenuVO>> parentIdMap) {
        List<MenuVO> children = parentIdMap.getOrDefault(menu.getId(), new ArrayList<>());
        menu.setChildren(children);

        // 递归设置子菜单的子菜单
        children.forEach(child -> setChildren(child, parentIdMap));
    }

    @Override
    public PageVO<MenuEntity> pageMenu(PageDTO request) {
        Page<MenuEntity> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<MenuEntity> queryWrapper = new LambdaQueryWrapper<>();

        // 关键字查询
        if (StrUtil.isNotBlank(request.getKeyword())) {
            queryWrapper.like(MenuEntity::getMenuName, request.getKeyword())
                    .or().like(MenuEntity::getMenuUrl, request.getKeyword());
        }

        // 排序
        queryWrapper.orderByAsc(MenuEntity::getId);

        IPage<MenuEntity> result = page(page, queryWrapper);

        return PageVO.build(result);
    }

    @Override
    public MenuEntity getMenuById(Long id) {
        return getById(id);
    }

    @Override
    public List<MenuVO> getMenusByRoleIds(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return new ArrayList<>();
        }

        // 根据角色ID列表查询菜单列表
        List<MenuEntity> menus = baseMapper.selectMenusByRoleIds(roleIds);

        // 将列表转为树形结构
        return buildTree(menus, null);
    }

    @Override
    public List<Long> getMenuIdsByRoleIds(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return new ArrayList<>();
        }

        // 根据角色ID列表查询菜单列表
        List<MenuEntity> menus = baseMapper.selectMenusByRoleIds(roleIds);
        return menus.stream().map(MenuEntity::getId).distinct().collect(Collectors.toList());
    }
} 