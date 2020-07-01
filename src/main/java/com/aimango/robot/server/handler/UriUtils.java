package com.aimango.robot.server.handler;

public class UriUtils {
    /**
     * 判断一个字符串是不是uri如果不是 尝试变成uri
     * @param uri
     * @return
     */
    public static String uri(String uri){
        if (!uri.startsWith("/")){
            uri = "/".concat(uri);
        }
        return uri;
    }
}
