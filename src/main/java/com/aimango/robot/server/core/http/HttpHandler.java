package com.aimango.robot.server.core.http;

import com.aimango.robot.server.core.container.HttpClassContainer;
import com.aimango.robot.server.core.launcher.HttpServerLauncher;
import com.aimango.robot.server.core.annotation.RequestMapping;
import com.aimango.robot.server.core.annotation.rest.RestRequestMapping;
import com.aimango.robot.server.core.interceptor.Interceptor;
import com.aimango.robot.server.core.interceptor.InterceptorRegistration;
import com.alibaba.fastjson.JSON;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

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
        HttpClassContainer httpClassContainer = (HttpClassContainer) HttpServerLauncher.getContainer();
        String uri = fullHttpRequest.uri();
        int indexOf = uri.indexOf("?");
        String uriSub;
        if (indexOf != -1) {
            uriSub = uri.substring(0, indexOf);
        } else {
            uriSub = uri;
        }

        Method method = httpClassContainer.getMethodByUri(uriSub);
        if (method != null) {

            boolean pass = postHttpHandler(httpClassContainer, uriSub, method);
            if (pass) {
                FullHttpResponse fullHttpResponse;
                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                String httpMethod = requestMapping.method();
                Object executor = httpClassContainer.getExecutorByMethod(method);
                try {
                    Object response = HttpMethodInvoke.valueOf(httpMethod.toUpperCase()).invoke(uriSub, executor, method, fullHttpRequest, false);
                    if (response instanceof FullHttpResponse) {
                        fullHttpResponse = (FullHttpResponse) response;
                    } else {
                        String res = JSON.toJSONString(response);
                        fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(res.getBytes()));
                        fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
                    }
                    if (httpClassContainer.isInterceptor()) {
                        afterHandler(httpClassContainer, fullHttpRequest, fullHttpResponse, method, null,uriSub);
                    }

                } catch (Exception e) {
                    fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.wrappedBuffer(e.getMessage().getBytes()));
                    fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
                    if (httpClassContainer.isInterceptor()) {
                        afterHandler(httpClassContainer, fullHttpRequest, fullHttpResponse, method, e,uriSub);
                    }
                }
                sendHttpResponse(fullHttpResponse);
            }else {
                send500Response("非法请求");
            }
            return null;
        }
        Method restMethod = httpClassContainer.getRestMethodByUri(uriSub);
        if (restMethod != null) {
            boolean pass = postHttpHandler(httpClassContainer, uriSub, restMethod);
            if (pass) {
                FullHttpResponse fullHttpResponse=null;
                RestRequestMapping restRequestMapping = restMethod.getAnnotation(RestRequestMapping.class);
                String httpMethod = restRequestMapping.method();
                Object executor = httpClassContainer.getRestExecutorByMethod(restMethod);
                try {
                    Object response = HttpMethodInvoke.valueOf(httpMethod.toUpperCase()).invoke(uriSub, executor, restMethod, fullHttpRequest, true);
                    if (response instanceof FullHttpResponse) {
                        fullHttpResponse = (FullHttpResponse) response;
                    } else {
                        String res = JSON.toJSONString(response);
                        fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(res.getBytes()));
                        fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
                    }
                    if (httpClassContainer.isInterceptor()) {
                        afterHandler(httpClassContainer, fullHttpRequest, fullHttpResponse, method, null,uriSub);
                    }
                } catch (Exception e) {
                    fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.wrappedBuffer(e.getMessage().getBytes()));
                    fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
                    if (httpClassContainer.isInterceptor()) {
                        afterHandler(httpClassContainer, fullHttpRequest, fullHttpResponse, method, e,uriSub);
                    }
                }
                sendHttpResponse(fullHttpResponse);
            }else {
                send500Response("非法请求");
            }
        }
        send404Response();

        return null;
    }

    private void afterHandler(HttpClassContainer httpClassContainer, FullHttpRequest request, FullHttpResponse response, Method method, Exception ex,String uriSub) {
        List<InterceptorRegistration> interceptorRegistrations = httpClassContainer.getInterceptorRegistrations();
        Iterator<InterceptorRegistration> iterator = interceptorRegistrations.iterator();
        while (iterator.hasNext()) {
            InterceptorRegistration interceptorRegistration = iterator.next();

            List<String> exludePatterns = interceptorRegistration.getExludePatterns();
            if (exludePatterns.size()>0){
                Iterator<String> stringIterator = exludePatterns.iterator();
                while (stringIterator.hasNext()) {
                    String exludeUrl = stringIterator.next();
                    if (!Pattern.matches(exludeUrl, uriSub)) {
                        List<String> includePatterns = interceptorRegistration.getIncludePatterns();
                        Iterator<String> iterator1 = includePatterns.iterator();
                        while (iterator1.hasNext()) {
                            String includeUrl = iterator1.next();
                            if (Pattern.matches(includeUrl, uriSub)) {
                                Interceptor interceptor = interceptorRegistration.getInterceptor();
                                interceptor.afterCompletion(fullHttpRequest,response,method,ex);
                            }
                        }
                    }
                }
            }else {
                List<String> includePatterns = interceptorRegistration.getIncludePatterns();
                Iterator<String> iterator1 = includePatterns.iterator();
                while (iterator1.hasNext()) {
                    String includeUrl = iterator1.next();
                    if (Pattern.matches(includeUrl, uriSub)) {
                        Interceptor interceptor = interceptorRegistration.getInterceptor();
                        interceptor.afterCompletion(fullHttpRequest,response,method,ex);
                    }
                }
            }

        }
    }

    private boolean postHttpHandler(HttpClassContainer httpClassContainer, String uriSub, Method method) {
        if (httpClassContainer.isInterceptor()) {
            List<InterceptorRegistration> interceptorRegistrations = httpClassContainer.getInterceptorRegistrations();
            Iterator<InterceptorRegistration> iterator = interceptorRegistrations.iterator();
            while (iterator.hasNext()) {
                InterceptorRegistration interceptorRegistration = iterator.next();

                List<String> exludePatterns = interceptorRegistration.getExludePatterns();
                if (exludePatterns.size()>0){
                    Iterator<String> stringIterator = exludePatterns.iterator();
                    while (stringIterator.hasNext()) {
                        String exludeUrl = stringIterator.next();
                        if (!Pattern.matches(exludeUrl, uriSub)) {
                            List<String> includePatterns = interceptorRegistration.getIncludePatterns();
                            Iterator<String> iterator1 = includePatterns.iterator();
                            while (iterator1.hasNext()) {
                                String includeUrl = iterator1.next();
                                if (Pattern.matches(includeUrl, uriSub)) {
                                    Interceptor interceptor = interceptorRegistration.getInterceptor();
                                    boolean pass = interceptor.postHandler(fullHttpRequest, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK), method);
                                    if (!pass) {
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }else {
                    List<String> includePatterns = interceptorRegistration.getIncludePatterns();
                    Iterator<String> iterator1 = includePatterns.iterator();
                    while (iterator1.hasNext()) {
                        String includeUrl = iterator1.next();
                        if (Pattern.matches(includeUrl, uriSub)) {
                            Interceptor interceptor = interceptorRegistration.getInterceptor();
                            boolean pass = interceptor.postHandler(fullHttpRequest, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK), method);
                            if (!pass) {
                                return false;
                            }
                        }
                    }
                }

            }
        }
        return true;
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

    private void sendHttpResponse(FullHttpResponse response) {
        channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
