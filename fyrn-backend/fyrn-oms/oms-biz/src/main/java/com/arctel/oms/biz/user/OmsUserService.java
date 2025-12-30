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
