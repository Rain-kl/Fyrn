package com.arctel.oms.pub.domain.output;


import lombok.Data;

import java.util.Date;

@Data
public class UserInfoVo {
    /**
     * 用户ID
     */
    private String userId;

    /**
     * 登录用户名
     */
    private String username;

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


}
