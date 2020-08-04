package com.aimango.robot.server.core.http;

import io.netty.handler.codec.http.FullHttpRequest;

import java.lang.reflect.Method;

public interface Invoker {
    Object invoke(String uri, Object executor, Method method, FullHttpRequest fullHttpRequest, boolean isRestful) throws Exception;
}
