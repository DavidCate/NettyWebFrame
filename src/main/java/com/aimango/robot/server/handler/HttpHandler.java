package com.aimango.robot.server.handler;

import com.aimango.robot.server.RobotSystemLauncher;
import com.aimango.robot.server.core.annotation.RequestMapping;
import com.aimango.robot.server.core.container.FullClassContainer;
import com.alibaba.fastjson.JSON;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class HttpHandler implements Callable {
    private ChannelHandlerContext channelHandlerContext;
    private FullHttpRequest fullHttpRequest;

    public HttpHandler(ChannelHandlerContext ctx, FullHttpRequest msg) {
        this.channelHandlerContext=ctx;
        this.fullHttpRequest=msg;
    }

    @Override
    public Object call() throws Exception {
        FullClassContainer fullClassContainer =(FullClassContainer) RobotSystemLauncher.getContainer();
        String uri = fullHttpRequest.uri();
        int indexOf = uri.indexOf("?");
        String uriSub;
        if (indexOf!=-1){
            uriSub = uri.substring(0, indexOf);
        }else {
            uriSub=uri;
        }
        Method method = fullClassContainer.getMethodByUri(uriSub);
        if (method!=null){
            RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
            String httpMethod = requestMapping.method();
            Object executor=fullClassContainer.getExecutorByMethod(method);
            Object response=HttpMethodInvoke.valueOf(httpMethod.toUpperCase()).invoke(executor,method,fullHttpRequest);
            sendHttpResponse(response);
        }else {
            send404Response();
        }


        return null;
    }

    private void send404Response() {
        HttpErrResponse httpErrResponse = new HttpErrResponse("未找到指定资源");
        String res = JSON.toJSONString(httpErrResponse);
        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, Unpooled.wrappedBuffer(res.getBytes()));
        fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);

        channelHandlerContext.writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
    }

    private void sendHttpResponse(Object response){
        String res = JSON.toJSONString(response);
        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(res.getBytes()));
        fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);

        channelHandlerContext.writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
    }
}
