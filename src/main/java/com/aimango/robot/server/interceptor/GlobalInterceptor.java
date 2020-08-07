package com.aimango.robot.server.interceptor;

import com.aimango.robot.server.core.interceptor.Interceptor;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import java.lang.reflect.Method;

public class GlobalInterceptor implements Interceptor {
    @Override
    public boolean postHandler(FullHttpRequest fullHttpRequest, FullHttpResponse fullHttpResponse, Method handlerMethod) {
        System.out.println("全局拦截器 posthandler");
        return true;
    }

    @Override
    public void afterCompletion(FullHttpRequest request, FullHttpResponse response, Object handler, Exception ex) {
        System.out.println("全局拦截器aftercompletion");
    }


}
