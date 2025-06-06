package com.siact.module.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.siact.module.base.dto.DicDTO;
import com.siact.module.base.dto.DicQuery;
import com.siact.module.base.entity.Dic;
import com.siact.module.base.mapper.DicMapper;
import com.siact.module.base.service.IDicService;
import com.siact.module.base.vo.DicVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 字典表 服务实现类
 * 
 * @author siact
 */
@Service
public class DicServiceImpl extends ServiceImpl<DicMapper, Dic> implements IDicService {
    
    @Override
    public DicVO selectDicById(Long id) {
        Dic dic = this.getById(id);
        return convertToVO(dic);
    }
    
    @Override
    public List<DicVO> selectDicList(DicQuery query) {
        if (query.getPageNum() != null && query.getPageSize() != null) {
            PageHelper.startPage(query.getPageNum(), query.getPageSize());
        }
        
        LambdaQueryWrapper<Dic> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(query.getType())) {
            wrapper.like(Dic::getType, query.getType());
        }
        
        if (StringUtils.hasText(query.getName())) {
            wrapper.like(Dic::getName, query.getName());
        }
        
        if (StringUtils.hasText(query.getCode())) {
            wrapper.like(Dic::getCode, query.getCode());
        }

        if (StringUtils.hasText(query.getTag())) {
            wrapper.eq(Dic::getTag, query.getTag());
        }
        
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Dic::getStatus, query.getStatus());
        }

        
        if (query.getBeginTime() != null && query.getEndTime() != null) {
            wrapper.between(Dic::getCreateTime, query.getBeginTime(), query.getEndTime());
        }
        
        // 排序
        if (StringUtils.hasText(query.getOrderByColumn())) {
            if ("asc".equals(query.getIsAsc())) {
                wrapper.orderByAsc(Dic::getSort);
            } else {
                wrapper.orderByDesc(Dic::getSort);
            }
        } else {
            wrapper.orderByAsc(Dic::getSort);
        }
        
        List<Dic> list = this.list(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }
    
    @Override
    public List<DicVO> selectDicByType(String type) {
        LambdaQueryWrapper<Dic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dic::getType, type);
        wrapper.orderByAsc(Dic::getSort);
        
        List<Dic> list = this.list(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }
    
    @Override
    public DicVO selectDicByTypeAndCode(String type, String code) {
        LambdaQueryWrapper<Dic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dic::getType, type);
        wrapper.eq(Dic::getCode, code);
        
        Dic dic = this.getOne(wrapper);
        return convertToVO(dic);
    }
    
    @Override
    public int insertDic(DicDTO dto) {
        Dic dic = new Dic();
        BeanUtils.copyProperties(dto, dic);
        
        return this.save(dic) ? 1 : 0;
    }
    
    @Override
    public int updateDic(DicDTO dto) {
        if (dto.getId() == null) {
            return 0;
        }
        
        Dic dic = new Dic();
        BeanUtils.copyProperties(dto, dic);
        
        return this.updateById(dic) ? 1 : 0;
    }
    
    @Override
    public int deleteDicByIds(Long[] ids) {
        return baseMapper.deleteBatchIds(Arrays.asList(ids));
    }
    
    @Override
    public int deleteDicById(Long id) {
        return this.removeById(id) ? 1 : 0;
    }

    @Override
    public List<DicVO> buildDicTree(List<DicVO> dicList) {
        List<DicVO> returnList = new ArrayList<>();
        Map<Long, DicVO> dicMap = new HashMap<>();
        
        // 准备数据，建立ID与字典的映射关系
        for (DicVO dic : dicList) {
            dicMap.put(dic.getId(), dic);
        }
        
        // 构建树形结构
        for (DicVO dic : dicList) {
            Long parentId = dic.getParentId();
            
            if (parentId == null || parentId == 0) {
                // 父节点为0或null的为顶级节点
                returnList.add(dic);
            } else {
                // 非顶级节点，添加到父节点的children列表
                DicVO parent = dicMap.get(parentId);
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(dic);
                }
            }
        }
        
        return returnList;
    }
    
    /**
     * 将实体转换为VO
     */
    private DicVO convertToVO(Dic dic) {
        if (dic == null) {
            return null;
        }
        
        DicVO vo = new DicVO();
        BeanUtils.copyProperties(dic, vo);
        return vo;
    }
} 