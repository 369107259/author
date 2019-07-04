package com.cn.author.common.annotation;

import java.lang.annotation.*;

/**
 * @Author 黄勇
 * @CreateTime 2019/7/2 17:52
 **/
@Documented
@Target({ElementType.METHOD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
    boolean required() default true;
}
