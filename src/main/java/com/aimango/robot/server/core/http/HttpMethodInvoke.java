package com.aimango.robot.server.core.http;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import com.aimango.robot.server.core.annotation.*;
import com.aimango.robot.server.core.annotation.validate.*;
import com.aimango.robot.server.core.container.Container;
import com.aimango.robot.server.core.container.HttpClassContainer;
import com.aimango.robot.server.core.container.IocContainer;
import com.aimango.robot.server.core.launcher.HttpServerLauncher;
import com.aimango.robot.server.core.annotation.rest.PathParam;
import com.aimango.robot.server.core.mybatis.Mybatis;
import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


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
                try {
                    if (!isRestful) {
                        Object o = HttpMethodInvoke.innerInvoke(executor, method, fullHttpRequest);
                        return o;
                    } else {
                        Object o = HttpMethodInvoke.restInnerInvoke(uri, executor, method, fullHttpRequest);
                        return o;
                    }
                } catch (IllegalArgumentException i) {
                    return i.getMessage();
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
        public Object invoke(String uri, Object executor, Method method, FullHttpRequest fullHttpRequest, boolean isRestful) throws Exception {
            HttpMethod httpMethod = fullHttpRequest.method();
            boolean pass = HttpUtils.isMethodPass(HttpMethod.POST, httpMethod.name());
            if (pass) {
                try {
                    if (!isRestful) {
                        Object o = HttpMethodInvoke.innerInvoke(executor, method, fullHttpRequest);
                        return o;
                    } else {
                        Object o = HttpMethodInvoke.restInnerInvoke(uri, executor, method, fullHttpRequest);
                        return o;
                    }
                } catch (IllegalArgumentException i) {
                    return i.getMessage();
                }
            } else {
                return new HttpErrResponse("请求方法错误！");
            }
        }
    };

    private static Object restInnerInvoke(String uri, Object executor, Method method, FullHttpRequest fullHttpRequest) throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] realParamValues = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            boolean requestBody = parameter.isAnnotationPresent(RequestBody.class);
            if (requestBody) {
                boolean isJson = fullHttpRequest.headers().contains(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON, true);
                if (isJson) {
                    Object o = jsonParamTransfer(parameter, fullHttpRequest);
                    realParamValues[i] = o;
                } else {
                    throw new IllegalArgumentException("非json格式请求参数!拒绝访问！");
                }
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
            if (pathParam) {
                PathParam annotation = parameter.getAnnotation(PathParam.class);
                String paramName = annotation.value();
                HttpClassContainer container = (HttpClassContainer) HttpServerLauncher.getContainer();
                Map<String, Integer> restUriPathParamIndexInfoMap = container.getRestUriPathParamIndexInfoMap(uri);
                Integer index = restUriPathParamIndexInfoMap.get(paramName);
                if (index != null) {
                    String[] strings = uri.split("/");
                    String real = strings[index];
                    Class type = parameter.getType();
                    if (type == String.class) {

                        realParamValues[i] = real;
                    }
                    if (type == Integer.class || type == int.class) {
                        realParamValues[i] = Integer.parseInt(real);
                    }
                    if (type == Double.class || type == double.class) {
                        realParamValues[i] = Double.parseDouble(real);
                    }
                } else {
                    throw new Exception("框架rest接口处理解析异常，无法获取uri路径参数坐标，容器中rest api数据解析有问题，数据不一致");
                }
            }
            Class type = parameter.getType();
            if (type.isAssignableFrom(FullHttpRequest.class)) {
                realParamValues[i] = fullHttpRequest;
            }
            if (type.isAssignableFrom(FullHttpResponse.class)) {
                FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
                realParamValues[i] = fullHttpResponse;
            }
        }
        try {
            methodPreInvoke(executor);
            Object response = method.invoke(executor, realParamValues);
            methodAfterInvoke(executor);
            return response;
        } catch (Exception e) {
            methodExceptionInvoke(executor);
            throw e;
        }
    }

    private static Object cycleCheck(Object object, Field field) throws IllegalAccessException {
        if (object == null) {
            throw new IllegalArgumentException("字段:" + field.getName() + "为空！");
        }
        Class<?> clazz = field.getType();
        boolean annotationPresent = clazz.isAnnotationPresent(Validate.class);
        if (annotationPresent) {
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                boolean accessible = declaredField.isAccessible();
                if (!accessible) {
                    declaredField.setAccessible(true);
                }

                Annotation[] fieldAnnotations = declaredField.getDeclaredAnnotations();
                for (Annotation annotation : fieldAnnotations) {
                    Class<? extends Annotation> annotationType = annotation.annotationType();
                    if (annotationType.equals(Email.class)) {
                        String fieldString = String.valueOf(declaredField.get(object));
                        boolean email = Validator.isEmail(fieldString);
                        if (!email) {
                            throw new IllegalArgumentException("字段：" + declaredField.getName() + "邮箱格式错误！");
                        }
                    }
                    if (annotationType.equals(Mobile.class)) {
                        String fieldString = String.valueOf(declaredField.get(object));
                        boolean mobile = Validator.isMobile(fieldString);
                        if (!mobile) {
                            throw new IllegalArgumentException("字段：" + declaredField.getName() + "手机格式错误！");
                        }
                    }
                    if (annotationType.equals(NotEmpty.class)) {
                        String fieldString = String.valueOf(declaredField.get(object));
                        if (!StrUtil.isNotEmpty(fieldString) || declaredField.get(object) == null) {
                            throw new IllegalArgumentException("字段：" + declaredField.getName() + "值不能为空！");
                        }
                    }
                    if (annotationType.equals(NotNull.class)) {
                        Object fieldObj = declaredField.get(object);
                        Object o = declaredField.get(object);
                        cycleCheck(o, declaredField);
                        if (fieldObj == null) {
                            throw new IllegalArgumentException("字段：" + declaredField.getName() + "不能为null！");
                        }
                    }
                }

            }
            return object;
        } else {
            return object;
        }
    }

    private static Object cycleCheck(Object object, Parameter parameter) throws IllegalAccessException {
        Class<?> clazz = parameter.getType();
        boolean annotationPresent = clazz.isAnnotationPresent(Validate.class);
        if (annotationPresent) {
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                boolean accessible = declaredField.isAccessible();
                if (!accessible) {
                    declaredField.setAccessible(true);
                }
                Annotation[] fieldAnnotations = declaredField.getDeclaredAnnotations();
                for (Annotation annotation : fieldAnnotations) {
                    Class<? extends Annotation> annotationType = annotation.annotationType();
                    if (annotationType.equals(Email.class)) {
                        String fieldString = String.valueOf(declaredField.get(object));
                        boolean email = Validator.isEmail(fieldString);
                        if (!email) {
                            throw new IllegalArgumentException("字段：" + declaredField.getName() + "邮箱格式错误！");
                        }
                    }
                    if (annotationType.equals(Mobile.class)) {
                        String fieldString = String.valueOf(declaredField.get(object));
                        boolean mobile = Validator.isMobile(fieldString);
                        if (!mobile) {
                            throw new IllegalArgumentException("字段：" + declaredField.getName() + "手机格式错误！");
                        }
                    }
                    if (annotationType.equals(NotEmpty.class)) {
                        String fieldString = String.valueOf(declaredField.get(object));
                        if (!StrUtil.isNotEmpty(fieldString) || declaredField.get(object) == null) {
                            throw new IllegalArgumentException("字段：" + declaredField.getName() + "值不能为空！");
                        }
                    }
                    if (annotationType.equals(NotNull.class)) {
                        Object fieldObj = declaredField.get(object);
                        if (fieldObj == null) {
                            throw new IllegalArgumentException("字段：" + declaredField.getName() + "不能为null！");
                        }
                        cycleCheck(fieldObj, declaredField);
                    }
                }

            }
            return object;
        } else {
            return object;
        }
    }

    private static Object jsonParamTransfer(Parameter parameter, FullHttpRequest fullHttpRequest) throws IllegalArgumentException, IllegalAccessException {
        try {
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
            cycleCheck(o, parameter);
            return o;
        } catch (IllegalAccessException e) {
            throw e;
        }
    }


    private static Object innerInvoke(Object executor, Method method, FullHttpRequest fullHttpRequest) throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] realParams = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            boolean requestBody = parameter.isAnnotationPresent(RequestBody.class);
            if (requestBody) {
                boolean isJson = fullHttpRequest.headers().contains(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON, true);
                if (isJson) {
                    Object o = jsonParamTransfer(parameter, fullHttpRequest);
                    realParams[i] = o;
                } else {
                    throw new IllegalArgumentException("非json格式请求参数!拒绝访问！");
                }
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
            if (type.isAssignableFrom(FullHttpResponse.class)) {
                FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
                realParams[i] = fullHttpResponse;
            }
        }
        try {
            methodPreInvoke(executor);
            Object response = method.invoke(executor, realParams);
            methodAfterInvoke(executor);
            return response;
        } catch (Exception e) {
            methodExceptionInvoke(executor);
            throw e;
        }
    }

    private static void executorAutowired(Object executor) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        IocContainer iocContainer = (IocContainer) HttpServerLauncher.getContainer();
        Class executorClass = executor.getClass();
        classAutowiredInstance(executorClass, iocContainer,executor ,executor);
    }

    /**
     * 对class进行实例化填充
     * @param clazz
     * @param iocContainer
     * @param o  被填充的对象
     * @return 返回填充完之后的对象
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    private static Object classAutowiredInstance(Class clazz, IocContainer iocContainer, Object o,Object executor) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        boolean anInterface = clazz.isInterface();
        if (anInterface) {
           return interfaceAutowired(clazz, iocContainer, o, executor);
        } else {
            //要填充的类不是接口
            //获取要填充的类所需要的类
            Map<Class, Set<Class>> classRequiredClassesMap = iocContainer.getClassRequiredClassesMap();
            Set<Class> classes = classRequiredClassesMap.get(clazz);
            if (classes != null) {

                Iterator<Class> iterator = classes.iterator();
                while (iterator.hasNext()) {
                    Class requiredClass = iterator.next();
                    Object requiredInstance = classAutowiredInstance(requiredClass, iocContainer, o, executor);
                    int i = requiredClass.getName().lastIndexOf(".");
                    String substring = requiredClass.getName().substring(i + 1);
                    String substring1 = substring.substring(0, 1);
                    String name = substring1.toLowerCase().concat(substring.substring(1));
                    Field field = clazz.getDeclaredField(name);
                    if (!field.isAccessible()){
                        field.setAccessible(true);
                    }
                    field.set(o,requiredInstance);
                }
                return o;
            } else {
                Constructor declaredConstructor = clazz.getDeclaredConstructor();
                boolean accessible = declaredConstructor.isAccessible();
                if (!accessible) {
                    declaredConstructor.setAccessible(true);
                }
                Object obj = declaredConstructor.newInstance();
                return obj;
            }
        }
    }

    private static Object interfaceAutowired(Class clazz,IocContainer iocContainer,Object o ,Object executor) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        //要填充的类是个接口
        Map<Class, Class> interfaceImplMap = iocContainer.getInterfaceImplMap();
        Class interfaceImplClass = interfaceImplMap.get(clazz);
        if (interfaceImplClass != null) {
            Constructor declaredConstructor = interfaceImplClass.getDeclaredConstructor();
            boolean accessible = declaredConstructor.isAccessible();
            if (!accessible){
                declaredConstructor.setAccessible(true);
            }
            Object interfaceImplInstance = declaredConstructor.newInstance();
            return classAutowiredInstance(interfaceImplClass, iocContainer, interfaceImplInstance,executor);
        } else {
            boolean annotationPresent = clazz.isAnnotationPresent(Repository.class);
            if (annotationPresent) {
                Repository repository = (Repository) clazz.getDeclaredAnnotation(Repository.class);
                String value = repository.value();
                if ("mybatis".equals(value)) {
                    Object mapper = Mybatis.getMapper(clazz, executor);
                    return mapper;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    private static void methodPreInvoke(Object executor) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        executorAutowired(executor);
    }

    private static void methodAfterInvoke(Object executor) {
        Mybatis.commitAndCloseExecutorSqlSession(executor);
    }

    private static void methodExceptionInvoke(Object executor) {
        Mybatis.callback(executor);
    }
}
