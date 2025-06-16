package com.siact.module.permission.service.impl;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.exception.CustomException;
import com.siact.common.utils.ConvertUtils;
import com.siact.module.permission.dto.AssignPermissionsDTO;
import com.siact.module.permission.dto.PageDTO;
import com.siact.module.permission.dto.UserDTO;
import com.siact.module.permission.dto.UserUpdateDTO;
import com.siact.module.permission.entity.*;
import com.siact.module.permission.mapper.*;
import com.siact.module.permission.service.UserService;
import com.siact.module.permission.vo.PageVO;
import com.siact.module.permission.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户Service实现类
 *
 * @author example
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Value("${jwt.salt}")
    private String salt;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private UserOrganizationMapper userOrganizationMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private OrganizationMapper organizationMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveUser(UserDTO request) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(UserEntity::getAccount, request.getAccount());
        long count = baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            return false;
        }

        if (StrUtil.isBlank(request.getPassword())) {
            // 默认密码
            // ps:前端的加密逻辑,密码后面拼接siact,然后在进行MD5加密,这里默认密码123456
            request.setPassword(DigestUtils.md5DigestAsHex(("123456" + "siact").getBytes()));
        }

        UserEntity user = ConvertUtils.sourceToTarget(request, UserEntity.class);
        if (ObjectUtils.isEmpty(user.getAccount())) {
            // 如果用户已存在  则account为根据用户名拼音字母
            user.setAccount(PinyinUtil.getPinyin(user.getUsername(), ""));
        }

        // 密码加密
        String password = passwordEncoder.encode(request.getPassword() + salt);// 前端密文 + 盐值进行密码加密,并存储
        user.setPassword(password);

        // 默认值设置
        if (user.getStatus() == null) {
            user.setStatus(true);
        }
        boolean result = save(user);

        // 保存用户角色关联
        if (result && CollectionUtils.isNotEmpty(request.getRoleIds())) {
            userRoleMapper.batchInsert(user.getId(), request.getRoleIds());
        }

        // 保存用户组织关联
        if (result && CollectionUtils.isNotEmpty(request.getOrgIds())) {
            userOrganizationMapper.batchInsert(user.getId(), request.getOrgIds());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(UserUpdateDTO request) {
        if (request.getId() == null) {
            return false;
        }

        UserEntity user = getById(request.getId());
        if (user == null) {
            return false;
        }

        // 检查用户名是否重复
        if (!user.getUsername().equals(request.getUsername())) {
            long count = count(new LambdaQueryWrapper<UserEntity>()
                    .eq(UserEntity::getUsername, request.getUsername()));
            if (count > 0) {
                return false;
            }
        }

        BeanUtil.copyProperties(request, user);

        // 密码处理，仅在密码不为空时更新
        if (StrUtil.isNotBlank(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.info("密码未修改");
        }

        boolean result = updateById(user);

        // 更新用户角色关联
        if (result && request.getRoleIds() != null) {
            // 先删除旧的关联
            userRoleMapper.delete(new LambdaQueryWrapper<UserRoleEntity>()
                    .eq(UserRoleEntity::getUserId, user.getId()));

            // 保存新的关联
            if (CollUtil.isNotEmpty(request.getRoleIds())) {
                userRoleMapper.batchInsert(user.getId(), request.getRoleIds());
            }
        }

        // 更新用户组织关联
        if (result && request.getOrgIds() != null) {
            // 先删除旧的关联
            userOrganizationMapper.delete(new LambdaQueryWrapper<UserOrganizationEntity>()
                    .eq(UserOrganizationEntity::getUserId, user.getId()));

            // 保存新的关联
            if (CollUtil.isNotEmpty(request.getOrgIds())) {
                userOrganizationMapper.batchInsert(user.getId(), request.getOrgIds());
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long id) {
        // 删除用户角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<UserRoleEntity>()
                .eq(UserRoleEntity::getUserId, id));

        // 删除用户组织关联
        userOrganizationMapper.delete(new LambdaQueryWrapper<UserOrganizationEntity>()
                .eq(UserOrganizationEntity::getUserId, id));

        // 删除用户
        return removeById(id);
    }

    @Override
    public boolean deleteUserByIdList(List<Long> idList) {
        // 删除用户角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<UserRoleEntity>()
                .in(UserRoleEntity::getUserId, idList));

        // 删除用户组织关联
        userOrganizationMapper.delete(new LambdaQueryWrapper<UserOrganizationEntity>()
                .in(UserOrganizationEntity::getUserId, idList));

        // 删除用户
        return removeByIds(idList);
    }

    /**
     * 分页查询用户信息
     *
     * @param request 分页请求DTO
     * @return
     */
    @Override
    public PageVO<UserVO> pageUser(PageDTO request) {
        Page<UserEntity> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();
        // 姓名
        queryWrapper.like(StringUtils.isNotBlank(request.getUsername()), UserEntity::getUsername, request.getUsername());

        List<Long> userIdList = new ArrayList<>();
        // 角色查询下对应的人员信息
        if (StringUtils.isNotBlank(request.getRoleId())) {
            LambdaQueryWrapper<UserRoleEntity> roleMapper = new LambdaQueryWrapper<>();
            roleMapper.select(UserRoleEntity::getUserId);
            roleMapper.eq(StringUtils.isNotBlank(request.getRoleId()), UserRoleEntity::getRoleId, request.getRoleId());
            List<UserRoleEntity> userRoleEntities = userRoleMapper.selectList(roleMapper);
            userIdList = userRoleEntities.stream().map(UserRoleEntity::getUserId).distinct().collect(Collectors.toList());
            if (userIdList.isEmpty()) {
                return getresultPageVo(request);
            }
        }

        // 组织部门对应的人员信息
        if (StringUtils.isNotBlank(request.getOrgId())) {
            LambdaQueryWrapper<UserOrganizationEntity> orgMapper = new LambdaQueryWrapper<>();
            orgMapper.select(UserOrganizationEntity::getUserId);
            orgMapper.in(CollectionUtils.isNotEmpty(userIdList), UserOrganizationEntity::getUserId, userIdList);
            orgMapper.eq(StringUtils.isNotBlank(request.getOrgId()), UserOrganizationEntity::getOrgId, request.getOrgId());
            List<UserOrganizationEntity> userOrganizationEntities = userOrganizationMapper.selectList(orgMapper);
            userIdList = userOrganizationEntities.stream().map(UserOrganizationEntity::getUserId).distinct().collect(Collectors.toList());
            if (userIdList.isEmpty()) {
                return getresultPageVo(request);
            }
        }


        // 组织查询下对应的人员信息
        queryWrapper.in(CollectionUtils.isNotEmpty(userIdList), UserEntity::getId, userIdList);
        IPage<UserEntity> result = page(page, queryWrapper);
        List<UserEntity> records = result.getRecords();
        // 类型转换
        List<UserVO> users = ConvertUtils.sourceToTarget(records, UserVO.class);
        List<Long> userId = CollectionUtils.isNotEmpty(users) ?
                users.stream().map(UserVO::getId).collect(Collectors.toList()) : Collections.emptyList();

        // 获取用户组织信息
        LambdaQueryWrapper<UserOrganizationEntity> userOrgWrapper = new LambdaQueryWrapper<>();
        userOrgWrapper.in(CollectionUtils.isNotEmpty(userId), UserOrganizationEntity::getUserId, userId);
        List<UserOrganizationEntity> userOrgEntitieList = userOrganizationMapper.selectList(userOrgWrapper);
        if (ObjectUtils.isEmpty(userOrgEntitieList)) {
            log.error("用户组织信息不存在,userId:{}", userId);
            throw new CustomException("用户组织信息不存在,userId:{}" + userId);
        }
        // 获取所有用户的组织id信息
        List<Long> orgIds = CollectionUtils.isNotEmpty(userOrgEntitieList) ? userOrgEntitieList.stream().map(UserOrganizationEntity::getOrgId).collect(Collectors.toList()) : Collections.EMPTY_LIST;
        List<OrganizationEntity> orgList = organizationMapper.selectBatchIds(orgIds);
        // 获取用户组织信息
        Map<Long, List<Long>> userOrgMap = CollectionUtils.isNotEmpty(userOrgEntitieList) ? userOrgEntitieList.stream()
                .collect(Collectors.groupingBy(UserOrganizationEntity::getUserId,
                        Collectors.mapping(UserOrganizationEntity::getOrgId, Collectors.toList()))) : Collections.emptyMap();
        // 获取角色信息
        LambdaQueryWrapper<UserRoleEntity> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.in(CollectionUtils.isNotEmpty(userId), UserRoleEntity::getUserId, userId);
        List<UserRoleEntity> userRoleEntitieList = userRoleMapper.selectList(userRoleWrapper);
        if (ObjectUtils.isEmpty(userRoleEntitieList)) {
            log.error("用户角色信息不存在,userId:{}", userId);
            throw new CustomException("用户角色信息不存在,userId:{}" + userId);
        }
        // 获取所有的角色信息
        List<Long> roleIds = CollectionUtils.isNotEmpty(userRoleEntitieList) ? userRoleEntitieList.stream().map(UserRoleEntity::getRoleId).collect(Collectors.toList()) : Collections.EMPTY_LIST;
        List<RoleEntity> roleEntities = this.roleMapper.selectBatchIds(roleIds);
        // 获取对应用户的角色id
        Map<Long, List<Long>> userRoleMap = CollectionUtils.isNotEmpty(userRoleEntitieList) ? userRoleEntitieList.stream()
                .collect(Collectors.groupingBy(UserRoleEntity::getUserId,
                        Collectors.mapping(UserRoleEntity::getRoleId, Collectors.toList()))) : Collections.emptyMap();
        // 封装用户信息
        users.forEach(user -> {
            List<Long> orgId = userOrgMap.get(user.getId()); // 组织id
            List<Long> roleId = userRoleMap.get(user.getId()); // 角色id
            user.setOrgList(orgId != null ? orgList.stream().filter(org -> orgId.contains(org.getId())).collect(Collectors.toList()) : null);

            user.setRoleList(roleId != null ? roleEntities.stream().filter(role -> roleId.contains(role.getId())).collect(Collectors.toList()) : null);
        });
        IPage<UserVO> vo = new Page<>();
        vo.setRecords(users);
        vo.setTotal(result.getTotal());
        return PageVO.build(vo);
    }

    @NotNull
    private static PageVO<UserVO> getresultPageVo(PageDTO request) {
        IPage<UserVO> vo = new Page<>(request.getPageNum(), request.getPageSize());
        vo.setRecords(Collections.emptyList());
        vo.setTotal(0L);
        return PageVO.build(vo);
    }

    @Override
    public UserVO getUserById(Long id) {
        UserEntity user = baseMapper.selectById(id);
        UserVO userVO = ConvertUtils.sourceToTarget(user, UserVO.class);
        if (userVO != null) {
            // 查询用户关联的角色ID列表
            List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(id);
            List<RoleEntity> roleEntities = roleMapper.selectBatchIds(roleIds);
            userVO.setRoleList(roleEntities);

            // 查询用户关联的组织ID列表
            List<Long> orgIds = userOrganizationMapper.selectOrgIdsByUserId(id);
            List<OrganizationEntity> orgEntities = organizationMapper.selectBatchIds(orgIds);
            userVO.setOrgList(orgEntities);

        }
        return userVO;
    }

    @Override
    public UserEntity getUserByAccount(String account) {
        UserEntity user = getOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getAccount, account)
                .eq(UserEntity::getStatus, true), false);

        if (user != null) {
            // 查询用户关联的角色ID列表
            List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(user.getId());
            user.setRoleIds(roleIds);

            // 查询用户关联的组织ID列表
            List<Long> orgIds = userOrganizationMapper.selectOrgIdsByUserId(user.getId());
            user.setOrgIds(orgIds);
        }

        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissions(AssignPermissionsDTO request) {

        Long userId = request.getUserId();
        List<Long> roleIds = request.getRoleIds();
        List<Long> orgIds = request.getOrgIds();

        if (userId == null) {
            return false;
        }

        // 先删除旧的关联
        userRoleMapper.delete(new LambdaQueryWrapper<UserRoleEntity>().eq(UserRoleEntity::getUserId, userId));

        // 保存新的关联
        if (CollUtil.isNotEmpty(roleIds)) {
            userRoleMapper.batchInsert(userId, roleIds);
        }

        // 分配组织权限
        assignOrganizations(userId, orgIds);
        return true;
    }

    /**
     * 获取所有用户相关信息：用户信息和权限信息
     */
    @Override
    public List<UserDetails> loadAllUsers() {
        // 获取所有用户
        List<UserEntity> users = list();
        // 获取用户的角色信息
        List<Long> userIdList = users.stream().map(UserEntity::getId).collect(Collectors.toList());
        LambdaQueryWrapper<UserRoleEntity> wrapper = new LambdaQueryWrapper<UserRoleEntity>().in(CollectionUtils.isNotEmpty(userIdList), UserRoleEntity::getUserId, userIdList);
        List<UserRoleEntity> userRoleEntities = userRoleMapper.selectList(wrapper);
        // 获取对应用户的角色id
        Map<Long, List<Long>> userRoleMap = userRoleEntities.stream().collect(Collectors.groupingBy(UserRoleEntity::getUserId, Collectors.mapping(UserRoleEntity::getRoleId, Collectors.toList())));
        List<UserDetails> userDetailsList = new ArrayList<>();
        for (UserEntity user : users) {
            List roleIdList = userRoleMap.get(user.getId());
            UserDetails userDetails = buildUserDetails(user, roleIdList);
            userDetailsList.add(userDetails);
        }
        return userDetailsList;
    }



    @Override
    public UserDetails loadUserByAccount(String account) {
        UserEntity user = getUserByAccount(account);
        if (user != null) {
            return buildUserDetails(user, user.getRoleIds());
        }
        return null;
    }

    @Override
    public void importUsers(MultipartFile file) throws Exception {
        ImportParams params = new ImportParams();
        // 表头行数，默认为 1
        params.setHeadRows(1);

        List<UserExcelEntity> list = ExcelImportUtil.importExcel(
                file.getInputStream(),
                UserExcelEntity.class,
                params
        );

        List<UserEntity> userEntities = ConvertUtils.sourceToTarget(list, UserEntity.class);
        saveBatch(userEntities);
    }

    /**
     * 构建用户权限信息
     */
    private UserDetails buildUserDetails(UserEntity user, List roleIdList) {
        // 获取用户的权限
        List<GrantedAuthority> authorities = roleIdList != null ? (List<GrantedAuthority>) roleIdList.stream().map(roleId -> new SimpleGrantedAuthority(roleId.toString())).collect(Collectors.toList()) : Collections.EMPTY_LIST;
        // 构建用户权限信息
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getAccount())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }


    public void assignOrganizations(Long userId, List<Long> orgIds) {
        if (userId == null) {
            return;
        }

        // 先删除旧的关联
        userOrganizationMapper.delete(new LambdaQueryWrapper<UserOrganizationEntity>()
                .eq(UserOrganizationEntity::getUserId, userId));

        // 保存新的关联
        if (CollUtil.isNotEmpty(orgIds)) {
            userOrganizationMapper.batchInsert(userId, orgIds);
        }
    }
} 