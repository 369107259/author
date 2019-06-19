package com.cn.author.common.constant.interfaces;

public interface CommonConstant {

    /*请求返回状态*/
    Integer RT_ERROR_500 = 500;
    Integer RT_OK_200 = 200;

    /*请求header中token得KEY*/
    String X_ACCESS_TOKEN = "X-Access-Token";

    /*登陆用户信息缓存KEY前缀*/
    String PREFIX_USER_TOKEN = "PREFIX_USER_TOKEN_";
    /**
     * 登录用户拥有角色缓存KEY前缀
     */
    String LOGIN_USER_CACHE_RULES_ROLE = "loginUser_cacheRules::Roles_";
    /**
     * 登录用户拥有权限缓存KEY前缀
     */
    String LOGIN_USER_CACHE_RULES_PERMISSION = "loginUser_cacheRules::Permissions_";
    /**
     * 登录用户令牌缓存KEY前缀
     */
    int TOKEN_EXPIRE_TIME = 3600; //3600秒即是一小时


}
