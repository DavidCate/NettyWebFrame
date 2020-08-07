package com.aimango.robot.server.interceptor;

import com.aimango.robot.server.core.interceptor.Interceptor;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import java.lang.reflect.Method;

public class GlobalInterceptor implements Interceptor {
    @Override
    public void postHandler(FullHttpRequest fullHttpRequest, FullHttpResponse fullHttpResponse, Method handlerMethod) {

    }

    @Override
    public void afterCompletion(FullHttpRequest request, FullHttpResponse response, Object handler, Exception ex) {

    }


}
