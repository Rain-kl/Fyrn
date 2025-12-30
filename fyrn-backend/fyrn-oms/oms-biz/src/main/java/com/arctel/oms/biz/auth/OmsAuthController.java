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
    OmsUserService omsUserService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<String> register(UserCreateInput input) {
        return Result.success(omsUserService.createUser(input));
    }
}
