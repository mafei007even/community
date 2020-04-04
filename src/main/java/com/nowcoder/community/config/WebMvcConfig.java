package com.nowcoder.community.config;

import com.nowcoder.community.controller.interceptor.LoginRequiredInterceptor;
import com.nowcoder.community.controller.interceptor.LoginTicketInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author mafei007
 * @date 2020/4/3 23:09
 */

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private LoginTicketInterceptor loginTicketInterceptor;
    private LoginRequiredInterceptor loginRequiredInterceptor;

    public WebMvcConfig(LoginTicketInterceptor loginTicketInterceptor, LoginRequiredInterceptor loginRequiredInterceptor) {
        this.loginTicketInterceptor = loginTicketInterceptor;
        this.loginRequiredInterceptor = loginRequiredInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.jpg", "/**/*.png", "/**/*.jpeg");

        registry.addInterceptor(loginRequiredInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.jpg", "/**/*.png", "/**/*.jpeg");

    }

}
