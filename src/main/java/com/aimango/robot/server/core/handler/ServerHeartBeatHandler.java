package com.aimango.robot.server.core.handler;

import com.aimango.robot.server.core.exception.NoneException;
import com.aimango.robot.server.core.exception.OutboundTargetValueException;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerHeartBeatHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ServerHeartBeatHandler.class);
    private String heartBeatRequest = JSON.toJSONString(new HeartBeatResponse());

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            idleStateEventDispatcher(idleStateEvent, ctx);
        }
    }

    private void idleStateEventDispatcher(IdleStateEvent idleStateEvent, ChannelHandlerContext ctx) {
        if (idleStateEvent.state() == IdleState.READER_IDLE) {
            readIdleHandler(ctx);
        }
        if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
            writeIdleHandler(ctx);
        }
        if (idleStateEvent.state() == IdleState.ALL_IDLE) {
            allIdleHandler(ctx);
        }
    }

    /**
     * 读空闲处理
     *
     * @param ctx
     */
    private void readIdleHandler(ChannelHandlerContext ctx) {
        logger.debug("读空闲处理");
        String key = ctx.channel().id().toString();
        boolean exists = HeartbeatAttrbute.exists(key);
        if (exists) {
            try {
                HeartbeatAttrbute.validateAndAdd(key, 3);
                ctx.writeAndFlush(new TextWebSocketFrame(heartBeatRequest));
            } catch (OutboundTargetValueException e) {
                logger.debug(String.format("key:[%s]的心跳重试值超过3次",key));
                HeartbeatAttrbute.remove(key);
                logger.debug("关闭连接！！！！！！！！！！！！！！！！！！！！！！！！！！！！");
                ctx.channel().close();
            } catch (NoneException e) {
                HeartbeatAttrbute.put(key,1);
                ctx.writeAndFlush(new TextWebSocketFrame(heartBeatRequest));
            }
        } else {
            HeartbeatAttrbute.put(key, 1);
            ctx.writeAndFlush(new TextWebSocketFrame(heartBeatRequest));
        }


    }

    /**
     * 写空闲处理
     *
     * @param ctx
     */
    private void writeIdleHandler(ChannelHandlerContext ctx) {

    }

    /**
     * 读写空闲处理
     *
     * @param ctx
     */
    private void allIdleHandler(ChannelHandlerContext ctx) {

    }
}
