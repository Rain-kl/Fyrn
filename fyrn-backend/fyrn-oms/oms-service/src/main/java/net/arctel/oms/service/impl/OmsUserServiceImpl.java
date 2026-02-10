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

package net.arctel.oms.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.arctel.oms.common.base.BaseQueryPage;
import net.arctel.oms.common.constants.ErrorConstant;
import net.arctel.framework.exception.BizException;
import net.arctel.oms.entity.OmsUser;
import net.arctel.oms.mapper.OmsUserMapper;
import net.arctel.oms.input.UserCreateInput;
import net.arctel.oms.input.UserPageQueryInput;
import net.arctel.oms.input.UserUpdateInput;
import net.arctel.oms.output.UserInfoVo;
import net.arctel.oms.service.OmsAuthService;
import net.arctel.oms.service.OmsUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import jakarta.annotation.Resource;

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
        omsAuthService.updatePassword(omsUser.getUserId(), input.getPassword());
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
    public BaseQueryPage<UserInfoVo> listUsers(UserPageQueryInput input) {
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
            omsAuthService.updatePassword(input.getUserId(), input.getPassword());
        }
        this.updateById(omsUser);
        return omsUser.getUserId();
    }
}
