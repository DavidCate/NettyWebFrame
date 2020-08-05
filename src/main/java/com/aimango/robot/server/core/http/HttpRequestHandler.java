package com.aimango.robot.server.core.http;


import com.aimango.robot.server.core.handler.ServerHeartBeatHandler;
import com.aimango.robot.server.core.pool.HttpThreadPool;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 处理http请求
 */
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger logger = LoggerFactory.getLogger(HttpRequestHandler.class);

    private static final String WS = "ws";

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("处理http请求服务异常！", cause);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        String uri = fullHttpRequest.uri();
        String protocol = getHttpOrWs(uri);
        if (StringUtils.isNotEmpty(protocol) && WS.equals(protocol)) {
            ChannelPipeline pipeline = channelHandlerContext.pipeline();
            pipeline.addLast("websocket协议处理器", new WebSocketServerProtocolHandler("/ws"));
//            pipeline.addLast("TextWebsocket数据帧处理", new TextWebSocketFrameHandler());
            pipeline.addLast("空闲检测", new IdleStateHandler(10, 0, 0, TimeUnit.SECONDS));
            pipeline.addLast("心跳检测", new ServerHeartBeatHandler());
            channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
        } else {
            httpHandler(channelHandlerContext, fullHttpRequest);
        }
    }

    private void httpHandler(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        Future future = HttpThreadPool.submit(new HttpHandler(channelHandlerContext, fullHttpRequest));
        Object results = future.get();
        logger.debug(String.format("http处理结果:[%s]", results));
    }

    /**
     * 解决跨域
     *
     * @param channelHandlerContext
     */
    protected void crossDomain(ChannelHandlerContext channelHandlerContext) {
        logger.info("跨域请求处理");
        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer("".getBytes()));
        fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        fullHttpResponse.headers().add(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        fullHttpResponse.headers().add(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "DNT,X-Mx-ReqToken,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization");
        fullHttpResponse.headers().add(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,DELETE,PATCH,PUT,OPTIONS");
        channelHandlerContext.writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 分割子url
     *
     * @param uri
     * @return
     */
    private String getHttpOrWs(String uri) {
        String[] contents = uri.split("/");
        int length = contents.length;
        if (length < 2) {
            return "";
        } else {
            return contents[1];
        }
    }

    /**
     * 获取url中的参数
     *
     * @param url
     * @return
     */
    private Map<String, String> getUrlParam(String url) {
        Map<String, String> params = new HashMap<>();
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(url);
        Map<String, List<String>> parameters = queryStringDecoder.parameters();
        parameters.entrySet().forEach(entry -> {
            String key = entry.getKey();
            List<String> value = entry.getValue();
            String paramValue = value.get(0);
            params.put(key, paramValue);
        });
        return params;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {

    }

    private void closeConnection(ChannelHandlerContext channelHandlerContext) {
        channelHandlerContext.close();
    }
}
