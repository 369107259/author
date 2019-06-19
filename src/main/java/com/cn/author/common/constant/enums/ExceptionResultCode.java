package com.cn.author.common.constant.enums;


public enum ExceptionResultCode {


    /**用户错误列表**/
    _210802(210802,"创建人不存在，userIds:${userIds}"),
    _210803(210803,"用户不存在"),
    _210804(210804,"登陆账户非主账户"),
    ;

    private Integer code;
    private String desc;
    ExceptionResultCode(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    public Integer code(){
        return this.code;
    }
    public String desc(){
        return this.desc;
    }
}

