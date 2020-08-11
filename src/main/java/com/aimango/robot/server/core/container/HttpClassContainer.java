package com.aimango.robot.server.core.container;

import com.aimango.robot.server.core.annotation.Autowired;
import com.aimango.robot.server.core.annotation.Controller;
import com.aimango.robot.server.core.annotation.RequestMapping;
import com.aimango.robot.server.core.annotation.rest.RestController;
import com.aimango.robot.server.core.annotation.rest.RestRequestMapping;
import com.aimango.robot.server.core.component.UriUtils;
import com.aimango.robot.server.core.interceptor.InterceptorRegistration;
import com.aimango.robot.server.core.interceptor.InterceptorRegistry;
import com.aimango.robot.server.core.interceptor.WebConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class HttpClassContainer extends IocContainer implements RestHttpHandlerContainer{
    private static final Logger logger= LoggerFactory.getLogger(HttpClassContainer.class);

    /**
     * 通过uri获取对应的method
     */
    private Map<String,Method> uriMethodMap=new ConcurrentHashMap<>(128);

    /**
     * 通过method获取对应的实例对象
     */
    private Map<Method,Object> methodObjectMap=new ConcurrentHashMap<>(128);

    /**
     * 通过rest uri获取对应的method
     */
    private Map<String,Method> restUriMethodMap=new ConcurrentHashMap<>(128);

    /**
     * 通过rest method 获取其对应的实例对象
     */
    private Map<Method,Object> restMethodObjectMap=new ConcurrentHashMap<>(128);

    /**
     * 记录rest uri的路径参数的位置坐标
     */
    private Map<String,Map<String,Integer>> restUriPathParamIndexInfoMap=new ConcurrentHashMap(128);

    /**
     * web配置，拦截器
     */
    private List<InterceptorRegistration> interceptorRegistrations;

    private boolean interceptor=false;

    public HttpClassContainer(Set<Class> classes) throws Exception {
        super(classes);
        init();
    }

    private void init() throws Exception {
        Map<Class, Object> targetMap = getTargetMap();
        for (Map.Entry<Class,Object> entry:targetMap.entrySet()){
            analyzeClassByAnnotation(entry);
        }
        logger.info("容器构建完毕");
    }

    /**
     * 分离出所有的Controller
     */
    private void analyzeClassByAnnotation(Map.Entry<Class, Object> entry) throws IllegalAccessException, InstantiationException {
        boolean isWebConfiguration=false;
        Class clazz = entry.getKey();
        Class[] interfaces = clazz.getInterfaces();

        for (Type type:interfaces){
            String typeName = type.getTypeName();
            String name = WebConfiguration.class.getName();
            if (typeName.equals(name)){
                isWebConfiguration=true;
                break;
            }
        }

        if (isWebConfiguration){
            initInterceptors(entry);
            this.interceptor=true;
        }
        Annotation[] declaredAnnotations = clazz.getDeclaredAnnotations();
        List<Annotation> classAnnotations = Arrays.asList(declaredAnnotations);
        for (Annotation annotation:classAnnotations){
            if (annotation.annotationType()== Controller.class){
                initHttpHandlerContainer(entry);
            }
            if (annotation.annotationType()== RestController.class){
                initRestHttpHandlerContainer(entry);
            }
        }
    }

    private void initInterceptors(Map.Entry<Class, Object> entry) throws IllegalAccessException, InstantiationException {
        InterceptorRegistry interceptorRegistry=new InterceptorRegistry();
        WebConfiguration webConfigurationInstance = (WebConfiguration)entry.getValue();
        webConfigurationInstance.addInterceptors(interceptorRegistry);
        List<InterceptorRegistration> registrations = interceptorRegistry.getRegistrations();
        if (registrations.size()>0){
            registrations.get(0);
            InterceptorRegistration[] interceptorRegistrations = registrations.toArray(new InterceptorRegistration[registrations.size()]);
            Arrays.sort(interceptorRegistrations, (o1,o2)-> {
                    Integer order = o1.getOrder();
                    Integer order1 = o2.getOrder();
                    if (order>order1){
                        return 1;
                    }else
                    if (order<order1){
                        return -1;
                    }else {
                        return 0;
                    }
                });
            this.interceptorRegistrations = Arrays.asList(interceptorRegistrations);
        }
    }

    private void initRestHttpHandlerContainer(Map.Entry<Class, Object> entry) throws InstantiationException, IllegalAccessException {
        Class clazz = entry.getKey();
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (Method method:declaredMethods){
            restMethodHandler(method,entry);
        }
    }

    private void restMethodHandler(Method method, Map.Entry<Class, Object> entry) throws IllegalAccessException, InstantiationException {
        boolean annotationPresent = method.isAnnotationPresent(RestRequestMapping.class);
        if (annotationPresent){
            RestRequestMapping annotation = method.getAnnotation(RestRequestMapping.class);
            String url = annotation.url();
            url = UriUtils.uri(url);
            String[] strings = url.split("/");
            Map<String,Integer> pathParamIndexMap=new HashMap<>();
            String uriRegex="/";
            for (int i = 0 ; i< strings.length;i++){
                String subString = strings[i];
                if (subString.startsWith("{")&&subString.endsWith("}")){
                    uriRegex+=".*";
                    String paramName = subString.substring(1, subString.length() - 1);
                    pathParamIndexMap.put(paramName,i);
                }else {
                    uriRegex+=subString;
                }
                if (!uriRegex.endsWith("/")){
                    uriRegex+="/";
                }
            }
            uriRegex=uriRegex.substring(0,uriRegex.length()-1);
            this.restUriPathParamIndexInfoMap.put(uriRegex,pathParamIndexMap);
            this.restUriMethodMap.put(uriRegex,method);
            this.restMethodObjectMap.put(method,entry.getValue());
        }
    }

    /**
     * 处理所有的@Controller类
     */
    private void initHttpHandlerContainer(Map.Entry<Class, Object> entry) throws InstantiationException, IllegalAccessException {
        Class clazz = entry.getKey();
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (Method method:declaredMethods){
            methodHandler(method,entry.getValue());
        }
    }

    /**
     * 处理Controller类的method
     */
    private void methodHandler(Method method, Object executor) throws IllegalAccessException, InstantiationException {
        boolean annotationPresent = method.isAnnotationPresent(RequestMapping.class);
        if (annotationPresent){
            RequestMapping annotation = method.getAnnotation(RequestMapping.class);
            String url = annotation.url();
            url = UriUtils.uri(url);
            this.uriMethodMap.put(url,method);
            this.methodObjectMap.put(method,executor);
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

    @Override
    public Method getRestMethodByUri(String uri) {
        Set<Map.Entry<String, Method>> entries = restUriMethodMap.entrySet();
        Iterator<Map.Entry<String, Method>> iterator = entries.iterator();
        while (iterator.hasNext()){
            Map.Entry<String, Method> next = iterator.next();
            String key = next.getKey();
            boolean matches = Pattern.matches(key, uri);
            if (matches){
                Method value = next.getValue();
                return value;
            }
        }
        return null;
    }

    @Override
    public Object getRestExecutorByMethod(Method restMethod) {
        Object o = restMethodObjectMap.get(restMethod);
        return o;
    }

    @Override
    public Map<String, Integer> getRestUriPathParamIndexInfoMap(String uri) {
        Set<Map.Entry<String, Map<String, Integer>>> entries = restUriPathParamIndexInfoMap.entrySet();
        for (Map.Entry<String,Map<String,Integer>> entry:entries){
            String key = entry.getKey();
            if (Pattern.matches(key,uri)){
                Map<String, Integer> value = entry.getValue();
                return value;
            }
        }
        return null;
    }

    public List<InterceptorRegistration> getInterceptorRegistrations() {
        return interceptorRegistrations;
    }

    public boolean isInterceptor() {
        return interceptor;
    }
}
