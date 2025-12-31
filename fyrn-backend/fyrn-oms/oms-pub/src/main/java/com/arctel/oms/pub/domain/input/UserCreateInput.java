package com.arctel.oms.pub.domain.input;

import lombok.Data;

@Data
public class UserCreateInput {

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

}
