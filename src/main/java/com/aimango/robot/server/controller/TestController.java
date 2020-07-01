package com.aimango.robot.server.controller;

import com.aimango.robot.server.core.annotation.Controller;
import com.aimango.robot.server.core.annotation.Param;
import com.aimango.robot.server.core.annotation.RequestBody;
import com.aimango.robot.server.core.annotation.RequestMapping;
import com.aimango.robot.server.pojo.Pojo;
import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;

@Controller
public class TestController {

    @RequestMapping(url = "xxx",method = RequestMapping.Method.POST)
    public Object test(FullHttpRequest fullHttpRequest, @Param("xxx")String a,@RequestBody Pojo pojo){
        System.out.println(a);
        System.out.println(JSON.toJSONString(pojo));
       return null;
    }
}
