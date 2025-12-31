package com.arctel.oms.biz.auth;

/**
* @author hspcadmin
* @description 针对表【oms_user(系统用户表)】的数据库操作Service
* @createDate 2025-12-30 14:04:20
*/
public interface OmsAuthService{


    String updatePassowrd(String userId, String password);

    Boolean login(String userId, String password);
}
