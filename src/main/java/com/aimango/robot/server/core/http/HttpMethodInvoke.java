package com.aimango.robot.server.core.http;

import com.aimango.robot.server.core.annotation.Repository;
import com.aimango.robot.server.core.container.Container;
import com.aimango.robot.server.core.container.HttpClassContainer;
import com.aimango.robot.server.core.container.IocContainer;
import com.aimango.robot.server.core.launcher.HttpServerLauncher;
import com.aimango.robot.server.core.annotation.MapperParam;
import com.aimango.robot.server.core.annotation.Param;
import com.aimango.robot.server.core.annotation.RequestBody;
import com.aimango.robot.server.core.annotation.rest.PathParam;
import com.aimango.robot.server.core.mybatis.Mybatis;
import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * @author DavidLiu
 */

/**
 * @author DavidLiu
 */

public enum HttpMethodInvoke implements Invoker {
    /**
     * GET请求校验
     */
    GET {
        @Override
        public Object invoke(String uri, Object executor, Method method, FullHttpRequest fullHttpRequest, boolean isRestful) throws Exception {
            HttpMethod httpMethod = fullHttpRequest.method();
            boolean pass = HttpUtils.isMethodPass(HttpMethod.GET, httpMethod.name());
            if (pass) {
                if (!isRestful) {
                    Object o = HttpMethodInvoke.innerInvoke(executor, method, fullHttpRequest);
                    return o;
                } else {
                    Object o = HttpMethodInvoke.restInnerInvoke(uri,executor, method, fullHttpRequest);
                    return o;
                }

            } else {
                return new HttpErrResponse("请求方法错误！");
            }
        }
    },
    /**
     * POST请求校验
     */
    POST {
        @Override
        public Object invoke(String uri,Object executor, Method method, FullHttpRequest fullHttpRequest, boolean isRestful) throws Exception {
            HttpMethod httpMethod = fullHttpRequest.method();
            boolean pass = HttpUtils.isMethodPass(HttpMethod.POST, httpMethod.name());
            if (pass) {
                if (!isRestful) {
                    Object o = HttpMethodInvoke.innerInvoke(executor, method, fullHttpRequest);
                    return o;
                } else {
                    Object o = HttpMethodInvoke.restInnerInvoke(uri, executor, method, fullHttpRequest);
                    return o;
                }
            } else {
                return new HttpErrResponse("请求方法错误！");
            }
        }
    };

    private static Object restInnerInvoke(String uri, Object executor, Method method, FullHttpRequest fullHttpRequest) throws Exception{
        Parameter[] parameters = method.getParameters();
        Object[] realParamValues=new Object[parameters.length];

        for (int i = 0 ; i<parameters.length;i++){
            Parameter parameter = parameters[i];
            boolean requestBody = parameter.isAnnotationPresent(RequestBody.class);
            if (requestBody) {
                Class type = parameter.getType();
                ByteBuf content = fullHttpRequest.content();
                int length = content.readableBytes();
                byte[] array = new byte[length];
                content.readBytes(array);
                String s = new String(array);
                if (StringUtils.isEmpty(s)) {
                    throw new IllegalArgumentException("RequestBody is Empty!");
                }
                Object o = JSON.parseObject(s, type);
                realParamValues[i] = o;
            }
            boolean param = parameter.isAnnotationPresent(Param.class);
            if (param) {
                Param annotation = parameter.getAnnotation(Param.class);
                String value = annotation.value();
                String url = fullHttpRequest.uri();
                QueryStringDecoder queryStringDecoder = new QueryStringDecoder(url);
                Map<String, List<String>> params = queryStringDecoder.parameters();
                List<String> values = params.get(value);
                if (values != null) {
                    Class type = parameter.getType();
                    if (type == String.class) {
                        realParamValues[i] = values.get(0);
                    }
                    if (type == Integer.class || type == int.class) {
                        realParamValues[i] = Integer.parseInt(values.get(0));
                    }
                    if (type == Double.class || type == double.class) {
                        realParamValues[i] = Double.parseDouble(values.get(0));
                    }
                }
            }
            boolean pathParam = parameter.isAnnotationPresent(PathParam.class);
            if (pathParam){
                PathParam annotation = parameter.getAnnotation(PathParam.class);
                String paramName = annotation.value();
                HttpClassContainer container =(HttpClassContainer) HttpServerLauncher.getContainer();
                Map<String, Integer> restUriPathParamIndexInfoMap = container.getRestUriPathParamIndexInfoMap(uri);
                Integer index = restUriPathParamIndexInfoMap.get(paramName);
                if (index!=null){
                    String[] strings = uri.split("/");
                    String real = strings[index];
                    Class type = parameter.getType();
                    if (type == String.class) {

                        realParamValues[i]=real;
                    }
                    if (type == Integer.class || type == int.class) {
                        realParamValues[i] = Integer.parseInt(real);
                    }
                    if (type == Double.class || type == double.class) {
                        realParamValues[i] = Double.parseDouble(real);
                    }
                }else {
                    throw new Exception("框架rest接口处理解析异常，无法获取uri路径参数坐标，容器中rest api数据解析有问题，数据不一致");
                }
            }
            Class type = parameter.getType();
            if (type.isAssignableFrom(FullHttpRequest.class)) {
                realParamValues[i] = fullHttpRequest;
            }
            if (type.isAssignableFrom(FullHttpResponse.class)){
                FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
                realParamValues[i]=fullHttpResponse;
            }
        }
        try {
            Object response = method.invoke(executor, realParamValues);
            return response;
        }finally {

        }
    }


    private static Object innerInvoke(Object executor, Method method, FullHttpRequest fullHttpRequest) throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] realParams = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            boolean requestBody = parameter.isAnnotationPresent(RequestBody.class);
            if (requestBody) {
                Class type = parameter.getType();
                ByteBuf content = fullHttpRequest.content();
                int length = content.readableBytes();
                byte[] array = new byte[length];
                content.readBytes(array);
                String s = new String(array);
                if (StringUtils.isEmpty(s)) {
                    throw new IllegalArgumentException("RequestBody is Empty!");
                }
                Object o = JSON.parseObject(s, type);
                realParams[i] = o;
            }
            boolean param = parameter.isAnnotationPresent(Param.class);
            if (param) {
                Param annotation = parameter.getAnnotation(Param.class);
                String value = annotation.value();
                String uri = fullHttpRequest.uri();
                QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
                Map<String, List<String>> params = queryStringDecoder.parameters();
                List<String> values = params.get(value);
                if (values != null) {
                    Class type = parameter.getType();
                    if (type == String.class) {
                        realParams[i] = values.get(0);
                    }
                    if (type == Integer.class || type == int.class) {
                        realParams[i] = Integer.parseInt(values.get(0));
                    }
                    if (type == Double.class || type == double.class) {
                        realParams[i] = Double.parseDouble(values.get(0));
                    }
                }
            }

            Class type = parameter.getType();
            if (type.isAssignableFrom(FullHttpRequest.class)) {
                realParams[i] = fullHttpRequest;
            }
            if (type.isAssignableFrom(FullHttpResponse.class)){
                FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
                realParams[i]=fullHttpResponse;
            }
        }
        try {
            methodPreInvoke(executor);
            Object response = method.invoke(executor, realParams);
            methodAfterInvoke(executor);
            return response;
        }finally {

        }
    }

    private static void methodPreInvoke(Object executor) throws NoSuchFieldException, IllegalAccessException {
        IocContainer iocContainer =(IocContainer) HttpServerLauncher.getContainer();
        Map<Object, List<Class>> objectClassListMap = iocContainer.getObjectClassListMap();
        List<Class> interfaces = objectClassListMap.get(executor);
        Iterator<Class> iterator = interfaces.iterator();
        while (iterator.hasNext()){
            Class interfaceImpl = iterator.next();
            boolean annotationPresent = interfaceImpl.isAnnotationPresent(Repository.class);
            if (annotationPresent){
                Repository repository=(Repository)interfaceImpl.getDeclaredAnnotation(Repository.class);
                String value = repository.value();
                if ("mybatis".equals(value)){
                    Object mapper = Mybatis.getMapper(interfaceImpl);

                    Field[] declaredFields = executor.getClass().getDeclaredFields();
                    for (Field field:declaredFields){
                        if (field.getType().equals(interfaceImpl)){
                            boolean accessible = field.isAccessible();
                            if (!accessible){
                                field.setAccessible(true);
                            }
                            field.set(executor,mapper);
                        }
                    }
                }
            }
        }
    }

    private static void methodAfterInvoke(Object executor){
        IocContainer iocContainer =(IocContainer) HttpServerLauncher.getContainer();
        Map<Object, List<Class>> objectClassListMap = iocContainer.getObjectClassListMap();
        List<Class> interfaces = objectClassListMap.get(executor);
        Iterator<Class> iterator = interfaces.iterator();
        while (iterator.hasNext()){
            Class interfaceImpl = iterator.next();
            boolean annotationPresent = interfaceImpl.isAnnotationPresent(Repository.class);
            if (annotationPresent){
                Repository repository=(Repository)interfaceImpl.getDeclaredAnnotation(Repository.class);
                String value = repository.value();
                if ("mybatis".equals(value)){
                    Object mapper = Mybatis.getMapper(interfaceImpl);
                    SqlSession sqlSession = Mybatis.getSqlSession(mapper);
                    sqlSession.commit();
                    Mybatis.remove(mapper);
                }
            }
        }
    }
}
