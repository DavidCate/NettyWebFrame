package com.aimango.robot.server.controller;

import com.aimango.robot.server.core.annotation.Param;
import com.aimango.robot.server.core.annotation.RequestBody;
import com.aimango.robot.server.core.annotation.rest.PathParam;
import com.aimango.robot.server.core.annotation.rest.RestController;
import com.aimango.robot.server.core.annotation.rest.RestRequestMapping;
import com.aimango.robot.server.pojo.Pojo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;

@RestController
public class TestRestController {

    @RestRequestMapping(url = "/{appId}/aaa",method = RestRequestMapping.Method.POST)
    public Object test(@PathParam("appId") String appId, @Param("bbb")String bbb, @RequestBody Pojo pojo, FullHttpRequest fullHttpRequest, FullHttpResponse fullHttpResponse){
        System.out.println(appId);
        System.out.println(bbb);
        System.out.println(JSON.toJSONString(pojo));
        JSONObject res=new JSONObject();
        res.put("aaa","xxx");
        return res;
    }

    @RestRequestMapping(url = "/{appId}/aaa1",method = RestRequestMapping.Method.POST)
    public Object test1(@PathParam("appId") String appId, @Param("bbb")String bbb, @RequestBody Pojo pojo, FullHttpRequest fullHttpRequest, FullHttpResponse fullHttpResponse){
        System.out.println(appId);
        System.out.println(bbb);
        System.out.println(JSON.toJSONString(pojo));
        fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        fullHttpResponse.content().writeBytes("xxx".getBytes());
        fullHttpResponse.content().writeBytes("\r\n".getBytes());
        fullHttpResponse.content().writeBytes(Unpooled.wrappedBuffer("xxxxx".getBytes()));
        return fullHttpResponse;
    }
}
