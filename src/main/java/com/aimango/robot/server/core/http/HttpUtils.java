package com.aimango.robot.server.core.http;

import cn.hutool.core.util.StrUtil;
import io.netty.handler.codec.http.HttpMethod;

public class HttpUtils {
    public static boolean isMethodPass(HttpMethod httpMethod, String name) {
        if (httpMethod.name().equals(name)){
            return true;
        }else {
            return false;

        }
    }
    public static class  ContentTypeJudge{
        public static final String APPLICATION_JSON="application/json";
        public static boolean isJson(String contentTypeValue){
            if (StrUtil.isNotEmpty(contentTypeValue)){
                boolean contains = contentTypeValue.contains(APPLICATION_JSON);
                return contains;
            }else {
                return false;
            }
        }
    }
}
