package com.siact.module.permission.controller;

import com.siact.common.result.R;
import com.siact.module.permission.dto.AssignPermissionsDTO;
import com.siact.module.permission.dto.PageDTO;
import com.siact.module.permission.dto.UserDTO;
import com.siact.module.permission.entity.UserEntity;
import com.siact.module.permission.service.UserService;
import com.siact.module.permission.vo.PageVO;
import com.siact.module.permission.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * 用户Controller
 *
 * @author example
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @ApiOperation(value = "新增用户")
    @PostMapping("save")
    public R<Boolean> save(@RequestBody @Validated UserDTO request) {

        R r;
        try {
            boolean result = userService.saveUser(request);
            if (!result) {
               r= R.fail("用户名已存在");
            }else {
                r = R.data();
            }
        }catch (Exception e){
            r = R.fail(e.getMessage());
        }
        return r;
    }

    @ApiOperation("修改用户")
    @PostMapping("update")
    public R<Boolean> update(@RequestBody @Validated UserDTO request) {
        boolean result = userService.updateUser(request);
        if (!result) {
            return R.fail("修改失败，可能用户名已存在");
        }
        return R.data(true);
    }

    @ApiOperation("删除用户")
    @ApiImplicitParam(name = "id", value = "用户ID", required = true, dataType = "Long", paramType = "path")
    @GetMapping("/delete/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        boolean result = userService.deleteUser(id);
        return R.data(result);
    }

    @ApiOperation("分页查询用户")
    @PostMapping("/page")
    public R<PageVO<UserVO>> page(@RequestBody PageDTO request) {
        PageVO<UserVO> page = userService.pageUser(request);
        return R.data(page);
    }

    @ApiOperation("获取用户详情")
    @ApiImplicitParam(name = "id", value = "用户ID", required = true, dataType = "Long", paramType = "path")
    @GetMapping("/get/{id}")
    public R<UserVO> getById(@PathVariable Long id) {
        UserVO user = userService.getUserById(id);
        return R.data(user);
    }

    @ApiOperation("根据用户名获取用户信息")
    @ApiImplicitParam(name = "username", value = "用户名", required = true, dataType = "String", paramType = "path")
    @GetMapping("/username/{username}")
    public R<UserEntity> getByUsername(@PathVariable String username) {
        UserEntity user = userService.getUserByUsername(username);
        return R.data(user);
    }

    @ApiOperation("分配权限")
    @PostMapping("/assign/permission")
    public R<Boolean> assignPermissions(@RequestBody AssignPermissionsDTO assignPermissionsDTO) {
        boolean result = userService.assignPermissions(assignPermissionsDTO);
        return R.data(result);
    }

    @ApiOperation("导入用户")
    @PostMapping("/import")
    public R<?> importUsers(@RequestParam("file") MultipartFile file) {
        try {
            userService.importUsers(file);
            return R.data();
        } catch (Exception e) {
            log.error("导入失败用户", e);
            return R.fail("导入失败：" + e.getMessage());
        }
    }
} 