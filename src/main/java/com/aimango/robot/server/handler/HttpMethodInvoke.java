package com.aimango.robot.server.handler;

import com.aimango.robot.server.core.annotation.Param;
import com.aimango.robot.server.core.annotation.RequestBody;
import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;


/**
 * @author DavidLiu
 */

public enum HttpMethodInvoke implements Invoker{
    /**
     * GET请求校验
     */
    GET{
        @Override
        public Object invoke(Object executor, Method method, FullHttpRequest fullHttpRequest) throws Exception {
            HttpMethod httpMethod = fullHttpRequest.method();
            boolean pass=HttpUtils.isMethodPass(HttpMethod.GET,httpMethod.name());
            if (pass){
                Object o = HttpMethodInvoke.innerInvoke(executor, method, fullHttpRequest);
                return o;
            }else {
                return new HttpErrResponse("请求方法错误！");
            }
        }
    },
    /**
     * POST请求校验
     */
    POST{
        @Override
        public Object invoke(Object executor, Method method, FullHttpRequest fullHttpRequest) throws Exception {
            HttpMethod httpMethod = fullHttpRequest.method();
            boolean pass=HttpUtils.isMethodPass(HttpMethod.POST,httpMethod.name());
            if (pass){
                Object o = HttpMethodInvoke.innerInvoke(executor, method, fullHttpRequest);
                return o;
            }else {
                return new HttpErrResponse("请求方法错误！");
            }
        }
    };


    private static Object innerInvoke(Object executor, Method method, FullHttpRequest fullHttpRequest) throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] realParams=new Object[parameters.length];
        for (int i=0;i<parameters.length;i++){
            Parameter parameter = parameters[i];
            boolean requestBody = parameter.isAnnotationPresent(RequestBody.class);
            if (requestBody){
                Class type = parameter.getType();
                ByteBuf content = fullHttpRequest.content();
                int length = content.readableBytes();
                byte[] array = new byte[length];
                content.readBytes(array);
                String s = new String(array);
                Object o = JSON.parseObject(s, type);
                realParams[i]=o;
            }
            boolean param = parameter.isAnnotationPresent(Param.class);
            if (param){
                Param annotation = parameter.getAnnotation(Param.class);
                String value = annotation.value();
                String uri = fullHttpRequest.uri();
                QueryStringDecoder queryStringDecoder=new QueryStringDecoder(uri);
                Map<String, List<String>> params = queryStringDecoder.parameters();
                List<String> values = params.get(value);
                if (values!=null){
                    Class type = parameter.getType();
                    if (type==String.class){
                        realParams[i]=values.get(0);
                    }
                    if (type==Integer.class||type==int.class){
                        realParams[i]=Integer.parseInt(values.get(0));
                    }
                    if (type==Double.class||type==double.class){
                        realParams[i]=Double.parseDouble(values.get(0));
                    }
                }
            }
            Class type = parameter.getType();
            if (type.isAssignableFrom(FullHttpRequest.class)){
                realParams[i]=fullHttpRequest;
            }
        }

        Object response = method.invoke(executor, realParams);
        return response;
    }
}
