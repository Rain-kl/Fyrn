package com.arctel.oms.pub.domain.input;

import lombok.Data;

@Data
public class AuthLoginInput {

    /**
     * 登录用户名
     */
    private String username;

    /**
     * 登录密码(加密)
     */
    private String password;

}
