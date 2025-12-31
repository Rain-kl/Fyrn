/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arctel.oms.biz.user;

import com.arctel.oms.pub.base.BaseQueryPage;
import com.arctel.oms.pub.domain.input.UserCreateInput;
import com.arctel.oms.pub.domain.input.QueryUserPageInput;
import com.arctel.oms.pub.domain.input.UserUpdateInput;
import com.arctel.oms.pub.domain.output.UserInfoVo;
import com.arctel.oms.pub.utils.Result;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * @author hspcadmin
 * @description 针对表【oms_user(系统用户表)】的数据库操作Service
 * @createDate 2025-12-30 14:04:20
 */
@RestController
@RequestMapping("/oms/user")
public class OmsUserController {

    @Resource
    OmsUserService omsUserService;

    /**
     * 创建用户
     */
    @PostMapping("/create")
    public Result<String> createUser(@RequestBody UserCreateInput input) {
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
    @GetMapping("/list")
    public Result<BaseQueryPage<UserInfoVo>> listUsers(QueryUserPageInput input) {
        return Result.success(omsUserService.listUsers(input));

    }

    /**
     * 更新用户信息
     */
    @PostMapping("/update")
    public Result<String> updateUser(@RequestBody UserUpdateInput input) {
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
