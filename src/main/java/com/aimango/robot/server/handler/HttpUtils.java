package com.aimango.robot.server.handler;

import io.netty.handler.codec.http.HttpMethod;

public class HttpUtils {
    public static boolean isMethodPass(HttpMethod httpMethod, String name) {
        if (httpMethod.name().equals(name)){
            return true;
        }else {
            return false;

        }
    }
}
