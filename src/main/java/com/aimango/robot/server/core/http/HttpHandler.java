package com.aimango.robot.server.core.http;


import com.aimango.robot.server.HttpServerLauncher;
import com.aimango.robot.server.core.annotation.RequestMapping;
import com.aimango.robot.server.core.annotation.rest.RestRequestMapping;
import com.aimango.robot.server.core.container.FullClassContainer;
import com.alibaba.fastjson.JSON;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class HttpHandler implements Callable {
    private ChannelHandlerContext channelHandlerContext;
    private FullHttpRequest fullHttpRequest;
    private static final Logger logger = LoggerFactory.getLogger(HttpHandler.class);

    public HttpHandler(ChannelHandlerContext ctx, FullHttpRequest msg) {
        this.channelHandlerContext = ctx;
        this.fullHttpRequest = msg;
    }

    @Override
    public Object call() throws Exception {
        FullClassContainer fullClassContainer = (FullClassContainer) HttpServerLauncher.getContainer();
        String uri = fullHttpRequest.uri();
        int indexOf = uri.indexOf("?");
        String uriSub;
        if (indexOf != -1) {
            uriSub = uri.substring(0, indexOf);
        } else {
            uriSub = uri;
        }
        Method method = fullClassContainer.getMethodByUri(uriSub);
        if (method != null) {
            RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
            String httpMethod = requestMapping.method();
            Object executor = fullClassContainer.getExecutorByMethod(method);
            try {
                Object response = HttpMethodInvoke.valueOf(httpMethod.toUpperCase()).invoke(uriSub,executor, method, fullHttpRequest,false);
                sendHttpResponse(response);
            } catch (IllegalArgumentException e) {
                logger.warn("不合法的请求参数", e);
                sendIlleagalArgumentResponse();
            }
            return null;
        }
        Method restMethod = fullClassContainer.getRestMethodByUri(uriSub);
        if (restMethod!=null){
            RestRequestMapping restRequestMapping = restMethod.getAnnotation(RestRequestMapping.class);
            String httpMethod = restRequestMapping.method();
            Object executor=fullClassContainer.getRestExecutorByMethod(restMethod);
            try {
                Object response = HttpMethodInvoke.valueOf(httpMethod.toUpperCase()).invoke(uriSub,executor, method, fullHttpRequest, true);
                sendHttpResponse(response);
            }catch (IllegalArgumentException e) {
                logger.warn("不合法的请求参数", e);
                sendIlleagalArgumentResponse();
            }
        }
        send404Response();
        return null;
    }

    private void sendIlleagalArgumentResponse() {
        send500Response("非法的请求参数！");
    }

    private void send500Response(String msg) {
        HttpErrResponse httpErrResponse = new HttpErrResponse(msg);
        String res = JSON.toJSONString(httpErrResponse);
        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.wrappedBuffer(res.getBytes()));
        fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        channelHandlerContext.writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
    }

    private void send404Response() {
        HttpErrResponse httpErrResponse = new HttpErrResponse("未找到指定资源");
        String res = JSON.toJSONString(httpErrResponse);
        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, Unpooled.wrappedBuffer(res.getBytes()));
        fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);

        channelHandlerContext.writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
    }

    private void sendHttpResponse(Object response) {
        if (response instanceof FullHttpResponse){
            channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }else {
            String res = JSON.toJSONString(response);
            FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(res.getBytes()));
            fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
            channelHandlerContext.writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
