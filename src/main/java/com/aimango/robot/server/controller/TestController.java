package com.aimango.robot.server.controller;

import com.aimango.robot.server.HttpServerLauncher;
import com.aimango.robot.server.core.annotation.*;
import com.aimango.robot.server.core.container.Container;
import com.aimango.robot.server.mapper.TestMapper;
import com.aimango.robot.server.pojo.Pojo;
import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;

@Controller
public class TestController {

    @RequestMapping(url = "/xxx",method = RequestMapping.Method.POST)
    public Object test(FullHttpRequest fullHttpRequest, @Param("xxx")String a, @RequestBody Pojo pojo, @MapperParam TestMapper testMapper){
        System.out.println(a);
        System.out.println(JSON.toJSONString(pojo));
        Container container = HttpServerLauncher.getContainer();
        container.getClass("");
        Integer integer = testMapper.select1();
        System.out.println(integer);
        return null;
    }
}
