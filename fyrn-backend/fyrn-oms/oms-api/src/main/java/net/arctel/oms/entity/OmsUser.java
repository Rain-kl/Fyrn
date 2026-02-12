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

package net.arctel.oms.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.arctel.oms.common.base.BaseQueryPage;
import net.arctel.oms.output.UserInfoVo;

/**
 * 系统用户表
 *
 * @TableName oms_user
 */
@TableName(value = "oms_user")
@Data
public class OmsUser {
    /**
     * 用户ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String userId;

    /**
     * 登录用户名
     */
    private String username;

    /**
     * 登录密码(加密)
     */
    private String password;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 用户状态：0=禁用，1=正常，2=锁定
     */
    private Integer status;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 创建人
     */
    private String createdUser;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;


    public static List<UserInfoVo> buildListUserInfoVoList(BaseQueryPage<OmsUser> userBaseQueryPage) {
        List<UserInfoVo> result = new ArrayList<>();
        for (OmsUser row : userBaseQueryPage.getRows()) {
            result.add(OmsUser.buildUserInfoVo(row));
        }
        return result;
    }

    public static UserInfoVo buildUserInfoVo(OmsUser omsUser) {
        return UserInfoVo.builder()
        		.userId(omsUser.getUserId())
        		.username(omsUser.getUsername())
        		.nickname(omsUser.getNickname())
        		.email(omsUser.getEmail())
        		.phone(omsUser.getPhone())
        		.status(omsUser.getStatus())
        		.lastLoginIp(omsUser.getLastLoginIp())
        		.lastLoginTime(omsUser.getLastLoginTime())
        		.createTime(omsUser.getCreateTime())
        		.updateTime(omsUser.getUpdateTime())
        		.build();

    }
}
