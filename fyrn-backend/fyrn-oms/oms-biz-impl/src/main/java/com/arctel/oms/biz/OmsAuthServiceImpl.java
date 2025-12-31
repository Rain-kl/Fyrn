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
        return true;
    }


    public void passCheck(String passwd) {
        if (passwd.length() < 8) {
            throw new BizException(ErrorConstant.CHECK_FAILED, "密码长度不能少于8位");
        }
    }
}
