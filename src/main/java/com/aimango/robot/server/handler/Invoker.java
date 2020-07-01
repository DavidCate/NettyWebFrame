package com.aimango.robot.server.handler;

import io.netty.handler.codec.http.FullHttpRequest;

import java.lang.reflect.Method;

public interface Invoker {
    Object invoke(Object executor, Method method, FullHttpRequest fullHttpRequest) throws Exception;
}
