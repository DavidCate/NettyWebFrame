package com.aimango.robot.server.handler;

import com.aimango.robot.server.core.thread.HttpThreadPool;
import com.aimango.robot.server.handler.heartbeat.ServerHeartBeatHandler;
import com.aimango.robot.server.handler.websocket.BinaryWebSocketFrameHandler;
import com.aimango.robot.server.handler.websocket.TextWebSocketFrameHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger logger= LoggerFactory.getLogger(HttpRequestHandler.class);
    private static final String WEBSOCKET_PROTOCOL="ws";
    private static final String HTTP_PROTOCOL="http";

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("全局异常捕获",cause);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        String protocol = protocol(msg);
        if (WEBSOCKET_PROTOCOL.equals(protocol)){
            try {
                websocketPreHandler(ctx,msg);
            }catch (Exception e){
                logger.error("websocket数据处理异常",e);
            }
        }else if (HTTP_PROTOCOL.equals(protocol)){
            try {
                httpPreHandler(ctx,msg);
            }catch (Exception e){
                logger.error("http数据处理异常",e);
            }
        }else {
            throw new Exception(String.format("未识别的uri->[%s]",msg.uri()));
        }
    }

    private void httpPreHandler(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        Future future = HttpThreadPool.submit(new HttpHandler(ctx, msg));
        Object results = future.get();
        logger.debug(String.format("http处理结果:[%s]",results));
    }

    private void websocketPreHandler(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest){
        ChannelPipeline pipeline = ctx.pipeline();
        pipeline.addLast("websocket协议处理器", new WebSocketServerProtocolHandler("/ws"));
        pipeline.addLast("二进制数据帧处理器", new BinaryWebSocketFrameHandler());
        pipeline.addLast("TextWebsocket数据帧处理", new TextWebSocketFrameHandler());
        pipeline.addLast("空闲检测", new IdleStateHandler(10, 0, 0, TimeUnit.SECONDS));
        pipeline.addLast("心跳检测", new ServerHeartBeatHandler());
        logger.info("有新的websocket请求");
        ctx.fireChannelRead(fullHttpRequest.retain());
    }


    private String protocol(FullHttpRequest fullHttpRequest){
        String uri = fullHttpRequest.uri();
        String[] split = uri.split("/");
        if (split.length>2&&WEBSOCKET_PROTOCOL.equals(split[2])){
            return WEBSOCKET_PROTOCOL;
        }else {
            return HTTP_PROTOCOL;
        }
    }
}
