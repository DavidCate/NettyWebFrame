package com.aimango.robot.server.core.initializer;

import com.aimango.robot.server.core.http.HttpRequestHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * channel处理器队列
 */
public class RobotServerInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger logger = LoggerFactory.getLogger(RobotServerInitializer.class);

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("http编解码器", new HttpServerCodec());
        pipeline.addLast("http大数据包处理器", new ChunkedWriteHandler());
        pipeline.addLast("http报文聚合器", new HttpObjectAggregator(64 * 1024));
        pipeline.addLast("自定义http请求分发器", new HttpRequestHandler());
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("服务全局异常捕获！", cause);
        ctx.close();
    }
}
