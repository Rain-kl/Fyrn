package com.arctel.oms.biz;

import com.arctel.oms.biz.mapper.OmsUserMapper;
import com.arctel.oms.biz.user.OmsUserService;
import com.arctel.oms.pub.base.BaseQueryPage;
import com.arctel.oms.pub.domain.OmsUser;
import com.arctel.oms.pub.domain.input.QueryUserPageInput;
import com.arctel.oms.pub.domain.input.UserCreateInput;
import com.arctel.oms.pub.domain.input.UserUpdateInput;
import com.arctel.oms.pub.domain.output.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author hspcadmin
 * @description 针对表【oms_user(系统用户表)】的数据库操作Service实现
 * @createDate 2025-12-30 14:04:20
 */
@Service
public class OmsUserServiceImpl extends ServiceImpl<OmsUserMapper, OmsUser>
        implements OmsUserService {

    @Override
    public String createUser(UserCreateInput input) {
        OmsUser omsUser = new OmsUser();
        BeanUtils.copyProperties(input, omsUser);
        // 安全因素, 修改密码操作使用专用接口
        omsUser.setPassword(null);
        this.save(omsUser);
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
    public String updateUser(UserUpdateInput input) {
        String userId = input.getUserId();
        OmsUser omsUser = getById(userId);
        BeanUtils.copyProperties(input, omsUser);
        omsUser.setPassword(null);
        this.updateById(omsUser);
        return omsUser.getUserId();
    }
}




