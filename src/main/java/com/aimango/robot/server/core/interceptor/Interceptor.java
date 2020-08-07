package com.aimango.robot.server.core.interceptor;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import java.lang.reflect.Method;

public interface Interceptor {
    boolean postHandler(FullHttpRequest fullHttpRequest, FullHttpResponse fullHttpResponse, Method handlerMethod);
    void afterCompletion(FullHttpRequest request, FullHttpResponse response, Object handler,Exception ex);
}
