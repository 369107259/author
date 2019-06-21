package com.cn.author.common.constant.interfaces;

/***
 * 公共参数
 * @author Administrator
 */
public interface CommonConstant {

    /***
     * 请求返回状态
     */
    Integer CODE_FAILED = 500;
    Integer CODE_SUCCESS = 200;
    String OPERATE_SUCCESS = "success";
    String OPERATE_FAILED = "failed";

    /***
     * 请求header中token得KEY
     */
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
     * //3600秒即是一小时
     */
    int TOKEN_EXPIRE_TIME = 3600;


}
