package com.cn.author;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.cn.author.*.mapper")
@EnableTransactionManagement
@EnableAspectJAutoProxy(proxyTargetClass = true,exposeProxy = true)
public class AuthorApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(AuthorApplication.class);
    }


    public static void main(String[] args) {
        SpringApplication.run(AuthorApplication.class, args);
    }

}


