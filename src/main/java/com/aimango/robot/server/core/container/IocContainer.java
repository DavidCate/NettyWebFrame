package com.aimango.robot.server.core.container;

import com.aimango.robot.server.core.annotation.Autowired;
import com.aimango.robot.server.core.annotation.Repository;
import com.aimango.robot.server.core.mybatis.Mybatis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class IocContainer extends ClassContainer {
    private static final Logger logger = LoggerFactory.getLogger(IocContainer.class);
    private volatile Map<Class, Object> objectMap = new ConcurrentHashMap<>(128);
    private volatile Map<Class, Object> tempMap = new ConcurrentHashMap<>(128);
    private volatile Map<Class, Object> targetMap = new ConcurrentHashMap<>(128);

    public IocContainer(Set<Class> classes) throws Exception {
        super(classes);
        init();
    }

    private void init() throws Exception {
        beanInit();
        dependencyInjection();
        refresh();
        logger.info("容器构建完毕");
    }

    /**
     * 读取objectMap进行di
     */
    private void dependencyInjection() throws Exception {
        for (Map.Entry<Class, Object> entry : objectMap.entrySet()) {
            boolean autowiredRequied = false;
            Class clazz = entry.getKey();
            Object object = entry.getValue();
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                boolean autowiredPresent = field.isAnnotationPresent(Autowired.class);
                if (autowiredPresent) {
                    autowiredRequied = true;
                    break;
                }
            }
            if (autowiredRequied) {
                tempMap.put(clazz, object);
            } else {
                targetMap.put(clazz, object);
            }
        }

        for (Map.Entry<Class, Object> entry : tempMap.entrySet()) {
            Class clazz = entry.getKey();
            Object object = entry.getValue();
            fillObject(clazz, object, null);
        }


    }

    /**
     * @param clazz  要填充的类
     * @param object 要填充的类的实例
     * @param filler 要填充的类的依赖者
     * @throws IllegalAccessException
     */
    private void fillObject(Class clazz, Object object, Class filler) throws Exception {
        if (!targetMap.containsKey(clazz)) {
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                boolean annotationPresent = field.isAnnotationPresent(Autowired.class);
                if (annotationPresent) {
                    Class fieldType = field.getType();
                    Object o = targetMap.get(fieldType);
                    if (o != null) {
                        boolean accessible = field.isAccessible();
                        if (!accessible) {
                            field.setAccessible(true);
                        }
                        field.set(object, o);
                    } else {
                        if (filler != null && filler.equals(fieldType)) {
                            throw new Exception("依赖注入发现循环依赖！请检查类:" + clazz.getName() + "和类:" + filler);
                        }
                        fillObject(fieldType, tempMap.get(fieldType), clazz);
                        Object fieldObject = targetMap.get(fieldType);
                        if (fieldObject != null) {
                            boolean accessible = field.isAccessible();
                            if (!accessible) {
                                field.setAccessible(true);
                            }
                            field.set(object, o);
                        } else {
                            throw new Exception("依赖不足！类:" + clazz.getName() + "依赖于类：" + field.getName() + "但是没有找到该依赖类的实例");
                        }
                    }
                }
            }
            targetMap.put(clazz,object);
        }
    }

    /**
     * 把所有component通过空参构造器进行实例化
     */
    private void beanInit() {
        //所有的@Component
        Set<Class> classes = getClasses();
        if (classes != null) {
            Iterator<Class> iterator = classes.iterator();
            while (iterator.hasNext()) {
                Class aClass = iterator.next();
                if (aClass.isInterface()) {
                    instanceInterface(aClass);
                } else {
                    instance(aClass);
                }
            }
        }
    }

    private void instanceInterface(Class aClass) {
        boolean annotationPresent = aClass.isAnnotationPresent(Repository.class);
        if (annotationPresent){

            Repository repository =(Repository) aClass.getDeclaredAnnotation(Repository.class);
            String value = repository.value();
            if ("mybatis".equals(value)){
                Object mapper = Mybatis.getMapper(aClass);
                targetMap.put(aClass,mapper);
            }


        }
    }

    private void refresh() {
        objectMap = null;
        tempMap = null;
    }

    private void instance(Class clazz) {
        try {
            Constructor constructor = clazz.getConstructor();
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            Object o = constructor.newInstance();
            objectMap.put(clazz, o);
        } catch (Exception e) {
            logger.error("不能够创建bean：" + clazz.getName() + ",请检查是否存在空参构造器");
        }
    }

    public Map<Class, Object> getTargetMap() {
        return targetMap;
    }
}
