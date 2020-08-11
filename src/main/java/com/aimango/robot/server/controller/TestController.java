package com.aimango.robot.server.controller;

import com.aimango.robot.server.core.launcher.HttpServerLauncher;
import com.aimango.robot.server.core.annotation.*;
import com.aimango.robot.server.core.container.Container;
import com.aimango.robot.server.core.mybatis.Mybatis;
import com.aimango.robot.server.mapper.TestMapper;
import com.aimango.robot.server.pojo.Pojo;
import com.aimango.robot.server.service.UserService;
import com.alibaba.fastjson.JSON;
import io.netty.handler.codec.http.FullHttpRequest;

@Controller
public class TestController {
    @Autowired
    A a;

    @Autowired
    TestMapper testMapper;

    @Autowired
    UserService userService;

    private Integer counts=0;

    @RequestMapping(url = "/xxx",method = RequestMapping.Method.POST)
    public Object test(FullHttpRequest fullHttpRequest, @Param("xxx")String a, @RequestBody Pojo pojo){
        System.out.println(a);
        System.out.println(JSON.toJSONString(pojo));
        Container container = HttpServerLauncher.getContainer();
        container.getClass("");
        Integer integer = testMapper.select1();
        System.out.println(integer);
        System.out.println(this.a.getAaa());
        System.out.println(userService.loginService());
        System.out.println("接口调用次数："+counts++);

        return null;
    }
}
