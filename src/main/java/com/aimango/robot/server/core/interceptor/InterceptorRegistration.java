package com.aimango.robot.server.core.interceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InterceptorRegistration {
    private Interceptor interceptor;
    private List<String> includePatterns=new ArrayList<>();
    private List<String> exludePatterns=new ArrayList<>();
    private Integer order;

    public InterceptorRegistration(Interceptor interceptor) {
        this.interceptor=interceptor;
    }

    public InterceptorRegistration addPathPatterns(String... pattern){
        List<String> list = Arrays.asList(pattern);
        includePatterns.addAll(list);
        return this;
    }

    public InterceptorRegistration addExludePathPatterns(String... pattern){
        List<String> list = Arrays.asList(pattern);
        exludePatterns.addAll(list);
        return this;
    }

    public InterceptorRegistration order(Integer order){
        this.order=order;
        return this;
    }

}
