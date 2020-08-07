package com.aimango.robot.server.configuration;

import com.aimango.robot.server.core.annotation.Component;
import com.aimango.robot.server.core.interceptor.InterceptorRegistry;
import com.aimango.robot.server.core.interceptor.WebConfiguration;
import com.aimango.robot.server.interceptor.GlobalInterceptor;

@Component
public class WebFilter implements WebConfiguration {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new GlobalInterceptor()).addPathPatterns("/**").order(1);
    }
}
