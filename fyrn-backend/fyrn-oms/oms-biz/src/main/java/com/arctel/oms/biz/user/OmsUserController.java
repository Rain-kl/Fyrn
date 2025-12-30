package com.arctel.oms.biz.user;

import com.arctel.oms.pub.base.BaseQueryPage;
import com.arctel.oms.pub.domain.input.UserCreateInput;
import com.arctel.oms.pub.domain.input.QueryUserPageInput;
import com.arctel.oms.pub.domain.input.UserUpdateInput;
import com.arctel.oms.pub.domain.output.UserInfoVo;
import com.arctel.oms.pub.utils.Result;

/**
 * @author hspcadmin
 * @description 针对表【oms_user(系统用户表)】的数据库操作Service
 * @createDate 2025-12-30 14:04:20
 */
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/oms/user")
public class OmsUserController {

    @Resource
    OmsUserService omsUserService;

    /**
     * 创建用户
     */
    @PostMapping
    public Result<String> createUser(UserCreateInput input) {
        return Result.success(omsUserService.createUser(input));
    }

    /**
     * 根据ID获取用户详情
     */
    @GetMapping("/{userId}")
    public Result<UserInfoVo> getUserById(@PathVariable("userId") String userId) {
        return Result.success(omsUserService.getUserById(userId));
    }

    /**
     * 分页查询用户列表
     */
    @GetMapping
    public Result<BaseQueryPage<UserInfoVo>> listUsers(QueryUserPageInput input) {
        return Result.success(omsUserService.listUsers(input));

    }

    /**
     * 更新用户信息
     */
    @PostMapping("/update")
    public Result<String> updateUser(UserUpdateInput input) {
        return Result.success(omsUserService.updateUser(input));

    }


    /**
     * 删除用户
     */
    @PostMapping("/delete/{userId}")
    public Result<Boolean> deleteUser(@PathVariable("userId") String userId) {
        boolean b = omsUserService.removeById(userId);
        return Result.success(b);
    }
}
