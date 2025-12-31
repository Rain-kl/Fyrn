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

import cn.hutool.crypto.digest.BCrypt;
import com.arctel.oms.biz.auth.OmsAuthService;
import com.arctel.oms.biz.mapper.OmsUserMapper;
import com.arctel.oms.pub.constants.ErrorConstant;
import com.arctel.oms.pub.domain.OmsUser;
import com.arctel.oms.pub.exception.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author hspcadmin
 * @description 针对表【oms_user(系统用户表)】的数据库操作Service
 * @createDate 2025-12-30 14:04:20
 */
@Service
public class OmsAuthServiceImpl implements OmsAuthService {

    @Resource
    private OmsUserMapper omsUserMapper;


    @Override
    public Boolean login(String userId, String password) {
        passCheck(password);
        OmsUser omsUser = omsUserMapper.selectOne(
                new LambdaQueryWrapper<OmsUser>()
                        .eq(OmsUser::getUserId, userId));
        if (omsUser == null) {
            throw new BizException(ErrorConstant.USER_NOT_FOUND, "用户不存在");
        }
        if (!BCrypt.checkpw(password, omsUser.getPassword())) {
            throw new BizException(ErrorConstant.COMMON_ERROR, "密码错误");
        }
        omsUser.setLastLoginTime(new Date());
        omsUserMapper.updateById(omsUser);
        return true;
    }


    @Override
    public String updatePassowrd(String userId, String password) {
        passCheck(password);
        String hashPasswd = BCrypt.hashpw(password, BCrypt.gensalt());
        OmsUser omsUser = omsUserMapper.selectOne(
                new LambdaQueryWrapper<OmsUser>()
                        .eq(OmsUser::getUserId, userId));
        if (omsUser == null) {
            throw new BizException(ErrorConstant.USER_NOT_FOUND, "用户不存在");
        }
        omsUser.setPassword(hashPasswd);
        omsUserMapper.updateById(omsUser);
        return userId;
    }


    public void passCheck(String passwd) {
        if (passwd.length() < 8) {
            throw new BizException(ErrorConstant.CHECK_FAILED, "密码长度不能少于8位");
        }
    }
}
