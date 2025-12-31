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
import com.arctel.oms.pub.domain.OmsUser;
import com.arctel.oms.pub.domain.input.UserCreateInput;
import com.arctel.oms.pub.domain.input.QueryUserPageInput;
import com.arctel.oms.pub.domain.input.UserUpdateInput;
import com.arctel.oms.pub.domain.output.UserInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.Serializable;

/**
* @author hspcadmin
* @description 针对表【oms_user(系统用户表)】的数据库操作Service
* @createDate 2025-12-30 14:04:20
*/
public interface OmsUserService extends IService<OmsUser> {

    @Override
    default boolean removeById(Serializable id) {
        return IService.super.removeById(id);
    }

    String createUser(UserCreateInput input);

    UserInfoVo getUserById(String userId);

    BaseQueryPage<UserInfoVo> listUsers(QueryUserPageInput input);

    String updateUser(UserUpdateInput input);
}
