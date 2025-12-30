package com.arctel.oms.pub.domain.input;

import com.arctel.oms.pub.base.BaseQueryPageInput;
import lombok.Data;

@Data
public class QueryUserPageInput extends BaseQueryPageInput {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 登录用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户状态：0=禁用，1=正常，2=锁定
     */
    private Integer status;
}
