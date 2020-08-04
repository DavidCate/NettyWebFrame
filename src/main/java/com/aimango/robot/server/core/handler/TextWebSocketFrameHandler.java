package com.aimango.robot.server.core.handler;//package com.aimango.server.core.handler;
//
//import com.aimango.robot.server.constant.MsgTypeConstant;
//import com.aimango.robot.server.exception.JsonParseException;
//import com.aimango.robot.server.exception.MsgTypeNotFoundException;
//import com.aimango.robot.server.exception.NotJsonException;
//import com.aimango.robot.server.handler.websocket.business.MsgTypeJudger;
//import com.aimango.robot.server.thread.WebSocketThreadPool;
//import com.aimango.robot.server.utils.ObjectUtils;
//import com.alibaba.fastjson.JSONObject;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.SimpleChannelInboundHandler;
//import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
//import io.netty.handler.codec.http.websocketx.WebSocketFrame;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.concurrent.Callable;
//import java.util.concurrent.Future;
//
///**
// * 判断websocket消息类型
// */
//public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
//    private static final Logger logger = LoggerFactory.getLogger(TextWebSocketFrameHandler.class);
//
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        logger.error("TextWebSocketFrameHandler异常！！！！！", cause);
//    }
//
//    /**
//     * 对文本消息进行类型判断，添加对应的处理器
//     *
//     * @param ctx
//     * @param webSocketFrame
//     * @throws Exception
//     */
//    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame webSocketFrame) {
//        if (webSocketFrame instanceof TextWebSocketFrame) {
//            TextWebSocketFrame textWebSocketFrame = (TextWebSocketFrame) webSocketFrame;
//            try {
//                doService(textWebSocketFrame, ctx);
//            } catch (Exception e) {
//                logger.error("websocket业务处理异常！", e);
//            }
//        }
//    }
//
//    private void doService(TextWebSocketFrame textWebSocketFrame, ChannelHandlerContext ctx) throws Exception {
//        String msg = textWebSocketFrame.text();
//        logger.info("接收到websocket文本数据:" + msg);
//        this.checkMsgIsJson(msg);
//        JSONObject jsonObject = this.parseJsonObject(msg);
//        String msgType = this.getMsgType(jsonObject);
//        String upperCase = msgType.toUpperCase();
//
//        Callable callable = MsgTypeJudger.valueOf(upperCase).judge(msg, ctx);
//        logger.info("新建线程提交处理");
//        Future future = WebSocketThreadPool.submit(callable);
//        future.get();
//    }
//
//    /**
//     * 检查字符串是否为json
//     *
//     * @param msg
//     * @throws NotJsonException
//     */
//    private void checkMsgIsJson(String msg) throws NotJsonException {
//        try {
//            ObjectUtils.parseObject(msg, JSONObject.class);
//        } catch (JsonParseException e) {
//            throw new NotJsonException(String.format("字符串[%s]不是json", msg), e);
//        }
//    }
//
//    /**
//     * 字符串转json对象
//     *
//     * @param str
//     * @return
//     * @throws Exception
//     */
//    private JSONObject parseJsonObject(String str) throws Exception {
//        try {
//            JSONObject jsonObject = ObjectUtils.parseObject(str, JSONObject.class);
//            return jsonObject;
//        } catch (JsonParseException e) {
//            throw new Exception(String.format("字符串[%s]转json对象失败!", str), e);
//        }
//    }
//
//    /**
//     * 获取消息的类型
//     *
//     * @param jsonObject
//     * @return
//     * @throws MsgTypeNotFoundException
//     */
//    private String getMsgType(JSONObject jsonObject) throws MsgTypeNotFoundException {
//        String msgType = jsonObject.getString(MsgTypeConstant.JSON_FIELD);
//        boolean notEmpty = StringUtils.isNotEmpty(msgType);
//        if (notEmpty) {
//            return msgType;
//        } else {
//            throw new MsgTypeNotFoundException(String.format("JSON:[%s]中未找到消息类型字段", jsonObject.toJSONString()));
//        }
//    }
//}
