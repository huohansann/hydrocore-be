package com.siact.module.permission.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.permission.dto.AssignPermissionsDTO;
import com.siact.module.permission.dto.PageDTO;
import com.siact.module.permission.dto.UserDTO;
import com.siact.module.permission.vo.PageVO;
import com.siact.module.permission.entity.UserEntity;
import com.siact.module.permission.vo.UserVO;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户Service接口
 *
 * @author example
 */
public interface UserService extends IService<UserEntity> {

    /**
     * 保存用户
     *
     * @param request 用户请求DTO
     * @return 是否成功
     */
    boolean saveUser(UserDTO request);

    /**
     * 更新用户
     *
     * @param request 用户请求DTO
     * @return 是否成功
     */
    boolean updateUser(UserDTO request);

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 是否成功
     */
    boolean deleteUser(Long id);

    /**
     * 批量删除用户
     *
     * @param idList 用户ID列表
     * @return 是否成功
     */
    boolean deleteUserByIdList(List<Long> idList);

    /**
     * 分页查询用户
     *
     * @param request 分页请求DTO
     * @return 分页结果
     */
    PageVO<UserVO> pageUser(PageDTO request);

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    UserVO getUserById(Long id);

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    UserEntity getUserByUsername(String username);

    /**
     * 分配权限：角色，组织机构
     *
     * @param request
     * @return
     */
    boolean assignPermissions(AssignPermissionsDTO request);

    /**
     * 获取所有用户信息
     *
     * @return
     */
    List<UserDetails> loadAllUsers();

    /**
     * 根据用户名获取用户信息
     *
     * @param username
     * @return
     */
    UserDetails loadUserByUsername(String username);

    void importUsers(MultipartFile file) throws Exception;

}