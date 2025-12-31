package com.arctel.oms.biz.auth;

import com.arctel.oms.biz.user.OmsUserService;
import com.arctel.oms.pub.base.BaseQueryPage;
import com.arctel.oms.pub.domain.input.QueryUserPageInput;
import com.arctel.oms.pub.domain.input.UserCreateInput;
import com.arctel.oms.pub.domain.input.UserUpdateInput;
import com.arctel.oms.pub.domain.output.UserInfoVo;
import com.arctel.oms.pub.utils.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/oms/auth")
public class OmsAuthController {

    @Resource
    OmsAuthService omsAuthService;

    @Resource
    OmsUserService omsUserService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<String> register(UserCreateInput input) {
        String userId = omsUserService.createUser(input);
        return Result.success(omsAuthService.updatePassowrd(userId,input.getPassword()));
    }
}
