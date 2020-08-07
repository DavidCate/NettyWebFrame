package com.aimango.robot.server.core.interceptor;

import java.util.ArrayList;
import java.util.List;

public class InterceptorRegistry {
    private List<InterceptorRegistration> registrations = new ArrayList<>();

    public InterceptorRegistration addInterceptor(Interceptor interceptor) {
        InterceptorRegistration interceptorRegistration = new InterceptorRegistration(interceptor);
        this.registrations.add(interceptorRegistration);
        return interceptorRegistration;
    }

    public List<InterceptorRegistration> getRegistrations(){
        return registrations;
    }
}
