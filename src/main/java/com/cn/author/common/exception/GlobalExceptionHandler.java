package com.cn.author.common.exception;

import com.cn.author.common.constant.interfaces.CommonConstant;
import com.cn.author.common.response.JsonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import springfox.documentation.annotations.ApiIgnore;

/**
 * @author huangyong
 */
@ApiIgnore
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public JsonResult handleException(Exception e) {
        log.error(e.getMessage(), e);
        return JsonResult.error500("服务器错误，请联系管理员");
    }

    /**
     * 拦截未知的运行时异常
     */
    @ExceptionHandler(value = RuntimeException.class)
    public JsonResult runtimeException(RuntimeException e) {
        log.error(e.getMessage(), e);
        return JsonResult.error500("运行时异常:" + e.getMessage());
    }

    /**
     * 请求方式不支持
     */
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public JsonResult handleException(HttpRequestMethodNotSupportedException e) {
        log.error(e.getMessage(), e);
        return JsonResult.error500("不支持' " + e.getMethod() + "'请求");
    }

    /***
     * 业务异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = BusinessException.class)
    public JsonResult businessException(BusinessException e) {
        log.error(e.getMessage(), e);
        return JsonResult.error500(null == e.getCode() ? CommonConstant.CODE_FAILED : e.getCode(), e.getMessage());
    }

}
