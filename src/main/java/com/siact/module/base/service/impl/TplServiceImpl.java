package com.siact.module.base.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.siact.module.base.dto.TplDTO;
import com.siact.module.base.dto.TplQuery;
import com.siact.module.base.entity.Tpl;
import com.siact.module.base.mapper.TplMapper;
import com.siact.module.base.service.TplService;
import com.siact.module.base.vo.TplVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 模板表 服务实现类
 * 
 * @author siact
 */
@Service
public class TplServiceImpl extends ServiceImpl<TplMapper, Tpl> implements TplService {
    
    @Override
    public TplVO selectTplById(Long id) {
        Tpl tpl = this.getById(id);
        return convertToVO(tpl);
    }
    
    @Override
    public List<TplVO> selectTplList(TplQuery query) {
        if (query.getPageNum() != null && query.getPageSize() != null) {
            PageHelper.startPage(query.getPageNum(), query.getPageSize());
        }
        
        LambdaQueryWrapper<Tpl> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(query.getTplName())) {
            wrapper.like(Tpl::getTplName, query.getTplName());
        }
        
        if (StringUtils.hasText(query.getTplCode())) {
            wrapper.like(Tpl::getTplCode, query.getTplCode());
        }
        
        if (StringUtils.hasText(query.getTplType())) {
            wrapper.eq(Tpl::getTplType, query.getTplType());
        }
        
        if (query.getBeginTime() != null && query.getEndTime() != null) {
            wrapper.between(Tpl::getCreateTime, query.getBeginTime(), query.getEndTime());
        }
        
        // 排序
        if (StringUtils.hasText(query.getOrderByColumn())) {
            if ("asc".equals(query.getIsAsc())) {
                wrapper.orderByAsc(Tpl::getId);
            } else {
                wrapper.orderByDesc(Tpl::getId);
            }
        } else {
            wrapper.orderByDesc(Tpl::getCreateTime);
        }
        
        List<Tpl> list = this.list(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public <T> List<T> getListByCode(String code, Class<T> clazz) {
        TplVO tpl = selectTplByCode(code);
        if (tpl == null) {
            return Collections.emptyList();
        }
        String tplContent = tpl.getTplContent();
        List<T> tplList = new ArrayList<>();
        if (tplContent.startsWith("[")) {
            tplList = JSONArray.parseArray(tplContent, clazz);
        } else {
            tplList.add(JSONObject.parseObject(tplContent, clazz));
        }
        return tplList;
    }

    @Override
    public List<TplVO> selectTplByType(String tplType) {
        LambdaQueryWrapper<Tpl> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tpl::getTplType, tplType);
        wrapper.orderByDesc(Tpl::getCreateTime);
        
        List<Tpl> list = this.list(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }
    
    @Override
    public TplVO selectTplByCode(String tplCode) {
        LambdaQueryWrapper<Tpl> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tpl::getTplCode, tplCode);
        
        Tpl tpl = this.getOne(wrapper);
        return convertToVO(tpl);
    }
    
    @Override
    public int insertTpl(TplDTO dto) {
        Tpl tpl = new Tpl();
        BeanUtils.copyProperties(dto, tpl);
        return this.save(tpl) ? 1 : 0;
    }
    
    @Override
    public int updateTpl(TplDTO dto) {
        if (dto.getId() == null) {
            return 0;
        }
        
        Tpl tpl = new Tpl();
        BeanUtils.copyProperties(dto, tpl);
        
        return this.updateById(tpl) ? 1 : 0;
    }
    
    @Override
    public int deleteTplByIds(Long[] ids) {
        return baseMapper.deleteBatchIds(Arrays.asList(ids));
    }
    
    @Override
    public int deleteTplById(Long id) {
        return this.removeById(id) ? 1 : 0;
    }
    
    /**
     * 将实体转换为VO
     */
    private TplVO convertToVO(Tpl tpl) {
        if (tpl == null) {
            return null;
        }
        
        TplVO vo = new TplVO();
        BeanUtils.copyProperties(tpl, vo);
        return vo;
    }
} 