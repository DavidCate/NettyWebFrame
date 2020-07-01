package com.aimango.robot.server.core.container;

import com.aimango.robot.server.core.annotation.Autowired;
import com.aimango.robot.server.core.annotation.Controller;
import com.aimango.robot.server.core.annotation.RequestMapping;
import com.aimango.robot.server.handler.UriUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FullClassContainer extends ClassContainer implements HttpHandlerContainer {
    private static final Logger logger= LoggerFactory.getLogger(FullClassContainer.class);

//    private HttpHandlerContainer httpHandlerContainer;

    /**
     * 通过uri获取对应的method
     */
    private Map<String,Method> uriMethodMap=new ConcurrentHashMap<>(128);

    /**
     * 通过method获取对应的实例对象
     */
    private Map<Method,Object> methodObjectMap=new ConcurrentHashMap<>(128);



    public FullClassContainer(Set<Class> classes) throws IllegalAccessException, InstantiationException {
        super(classes);
        init();
    }

    private void init() throws InstantiationException, IllegalAccessException {
        //所有的@Component
        Set<Class> classes = getClasses();
        if (classes!=null){
            Iterator<Class> iterator = classes.iterator();
            while (iterator.hasNext()){
                Class next = iterator.next();
                analyzeClassByAnnotation(next);
            }
        }
    }

    /**
     * 分离出所有的Controller
     * @param clazz
     */
    private void analyzeClassByAnnotation(Class clazz) throws IllegalAccessException, InstantiationException {
        Annotation[] declaredAnnotations = clazz.getDeclaredAnnotations();
        List<Annotation> classAnnotations = Arrays.asList(declaredAnnotations);
        for (Annotation annotation:classAnnotations){
            if (annotation.annotationType()==Controller.class){
                initHttpHandlerContainer(clazz);
            }
        }
    }

    /**
     * 处理所有的@Controller类
     * @param clazz
     */
    private void initHttpHandlerContainer(Class clazz) throws InstantiationException, IllegalAccessException {
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field:declaredFields){
            fieldHandler(field,clazz);
        }

        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (Method method:declaredMethods){
            methodHandler(method,clazz);
        }
    }

    /**
     * 处理Controller类的method
     */
    private void methodHandler(Method method, Class clazz) throws IllegalAccessException, InstantiationException {
        boolean annotationPresent = method.isAnnotationPresent(RequestMapping.class);
        if (annotationPresent){
            RequestMapping annotation = method.getAnnotation(RequestMapping.class);
            String url = annotation.url();
            url = UriUtils.uri(url);
            this.uriMethodMap.put(url,method);
            this.methodObjectMap.put(method,clazz.newInstance());
        }
    }

    /**
     * 处理Controller类的成员变量
     */
    private void fieldHandler(Field field, Class clazz) {
        boolean annotationPresent = field.isAnnotationPresent(Autowired.class);
        if (annotationPresent){
            Class declaringClass = field.getDeclaringClass();
        }
    }



    @Override
    public Method getMethodByUri(String uri) {
        Method method = uriMethodMap.get(uri);
        return method;
    }

    @Override
    public Method addMethodForUri(String uri, Class clazz) {
        return null;
    }

    /**
     * 通过method 得到method 的类的实例
     * @param method
     * @return
     */
    public Object getExecutorByMethod(Method method) {
        Object target = methodObjectMap.get(method);
        return target;
    }
}
