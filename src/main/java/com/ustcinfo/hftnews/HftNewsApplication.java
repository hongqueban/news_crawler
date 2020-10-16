package com.ustcinfo.hftnews;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * springboot启动类
 *
 * @author zhang.yifan
 * @date 2019/10/9 14:31
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.ustcinfo.hftnews.mapper")
public class HftNewsApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(HftNewsApplication.class, args);
    }

    /**
     * 为了打包springboot项目
     */
    @Override
    protected SpringApplicationBuilder configure(
            SpringApplicationBuilder builder) {
        return builder.sources(this.getClass());
    }
}
