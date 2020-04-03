package com.nowcoder.community.config;

import com.nowcoder.community.controller.interceptor.LoginTicketInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author mafei007
 * @date 2020/4/3 23:09
 */

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private LoginTicketInterceptor loginTicketInterceptor;

    public WebMvcConfig(LoginTicketInterceptor loginTicketInterceptor) {
        this.loginTicketInterceptor = loginTicketInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // js css jpg SpringBoot已经帮我做好了映射，不用在 exclude
        registry.addInterceptor(loginTicketInterceptor)
                .addPathPatterns("/**");

    }

}
