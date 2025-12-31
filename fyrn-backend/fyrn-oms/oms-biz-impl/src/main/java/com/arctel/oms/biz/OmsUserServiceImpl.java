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

package com.arctel.oms.biz;

import com.arctel.oms.biz.auth.OmsAuthService;
import com.arctel.oms.biz.mapper.OmsUserMapper;
import com.arctel.oms.biz.user.OmsUserService;
import com.arctel.oms.pub.base.BaseQueryPage;
import com.arctel.oms.pub.constants.ErrorConstant;
import com.arctel.oms.pub.domain.OmsUser;
import com.arctel.oms.pub.domain.input.QueryUserPageInput;
import com.arctel.oms.pub.domain.input.UserCreateInput;
import com.arctel.oms.pub.domain.input.UserUpdateInput;
import com.arctel.oms.pub.domain.output.UserInfoVo;
import com.arctel.oms.pub.exception.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author hspcadmin
 * @description 针对表【oms_user(系统用户表)】的数据库操作Service实现
 * @createDate 2025-12-30 14:04:20
 */
@Service
public class OmsUserServiceImpl extends ServiceImpl<OmsUserMapper, OmsUser>
        implements OmsUserService {

    @Resource
    OmsAuthService omsAuthService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createUser(UserCreateInput input) {
        String username = input.getUsername();
        String password = input.getPassword();
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new BizException(ErrorConstant.CHECK_FAILED, "用户名和密码不能为空");
        }
        OmsUser omsUser = new OmsUser();
        BeanUtils.copyProperties(input, omsUser);
        // 安全因素, 修改密码操作使用专用接口
        omsUser.setPassword(null);
        this.save(omsUser);
        omsAuthService.updatePassowrd(omsUser.getUserId(), input.getPassword());
        return omsUser.getUserId();
    }

    @Override
    public UserInfoVo getUserById(String userId) {
        OmsUser omsUser = this.getById(userId);
        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtils.copyProperties(omsUser, userInfoVo);
        return userInfoVo;
    }

    @Override
    public BaseQueryPage<UserInfoVo> listUsers(QueryUserPageInput input) {
        Integer pageNo = input.getPageNo();
        Integer pageSize = input.getPageSize();
        IPage<OmsUser> page = new Page<>(pageNo, pageSize);

        IPage<OmsUser> result = page(
                page,
                new LambdaQueryWrapper<OmsUser>()
                        .like(input.getUserId() != null,
                                OmsUser::getUserId, input.getUserId())
                        .like(input.getEmail() != null,
                                OmsUser::getEmail, input.getEmail())
                        .like(input.getUsername() != null,
                                OmsUser::getUsername, input.getUsername())
                        .eq(input.getStatus() != null,
                                OmsUser::getStatus, input.getStatus())
                        .orderByDesc(OmsUser::getUserId)
        );
        List<UserInfoVo> ordersList = result.getRecords().stream().map(omsUser -> {
            UserInfoVo userInfoVo = new UserInfoVo();
            BeanUtils.copyProperties(omsUser, userInfoVo);
            return userInfoVo;
        }).toList();
        return new BaseQueryPage<>(result.getTotal(), pageSize, pageNo, ordersList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateUser(UserUpdateInput input) {
        String userId = input.getUserId();
        if (StringUtils.isBlank(userId)) {
            throw new BizException(ErrorConstant.CHECK_FAILED, "用户ID不能为空");
        }
        OmsUser omsUser = getById(userId);
        BeanUtils.copyProperties(input, omsUser);
        omsUser.setPassword(null);
        if (StringUtils.isNotBlank(input.getPassword())) {
            omsAuthService.updatePassowrd(input.getUserId(), input.getPassword());
        }
        this.updateById(omsUser);
        return omsUser.getUserId();
    }
}




