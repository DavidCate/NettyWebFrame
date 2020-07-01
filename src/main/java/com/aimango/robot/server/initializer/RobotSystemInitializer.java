package com.aimango.robot.server.initializer;

import com.aimango.robot.server.handler.HttpRequestHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

public class RobotSystemInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("http编解码器", new HttpServerCodec());
        pipeline.addLast("http大数据包处理器", new ChunkedWriteHandler());
        pipeline.addLast("http报文聚合器", new HttpObjectAggregator(64 * 1024));
        pipeline.addLast("自定义http请求分发器", new HttpRequestHandler());
    }
}
