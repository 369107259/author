package com.cn.author.common.exception;


public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = -6716943269241933289L;
    private Integer code;

    public BusinessException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }
    
    public BusinessException(String msg) {
        super(msg);
    }

    public Integer getCode() {
        return code;
    }

}