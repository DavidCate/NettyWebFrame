package com.aimango.robot.server.core.container;

import com.aimango.robot.server.core.annotation.Autowired;
import com.aimango.robot.server.core.annotation.Component;
import com.aimango.robot.server.core.annotation.Repository;
import com.aimango.robot.server.core.component.ClassScanner;
import com.aimango.robot.server.core.mybatis.Mybatis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 对所有的component进行处理
 * <p>
 * 1.对所有的接口component进行记录
 * 2.对所有使用@Autowired的类进行关联
 * 3.对所有接口component的实现类进行关联
 * <p>
 * <p>
 * 请求打过来之后的流程
 * 找到url对应的method
 * 找到method对应的controller的class
 * 对controller的class进行装配
 * 调用方法
 */
public abstract class IocContainer extends ClassContainer {
    private static final Logger logger = LoggerFactory.getLogger(IocContainer.class);
    //新版
    //所有的component接口
    private volatile Set<Class> interfaces = new CopyOnWriteArraySet<>();
    //所有的component类
    private volatile Set<Class> classes = new CopyOnWriteArraySet<>();
    //所有的不需要装配的class
    private volatile Set<Class> target = new CopyOnWriteArraySet<>();
    //某个类需要装配的 接口和类
    private volatile Map<Class, Set<Class>> classRequiredClassesMap = new ConcurrentHashMap<>(128);
    //componet组件接口与他的实现类的映射
    private volatile Map<Class, Class> interfaceImplMap = new ConcurrentHashMap<>(128);


    public IocContainer(Set<Class> classes) throws Exception {
        super(classes);
        init();
    }

    private void init() throws Exception {
        classSeparateInit();
        dependencyAnalyze();
        refresh();
        logger.info("容器构建完毕");
    }

    /**
     * 对所有的component class进行解析
     */
    private void dependencyAnalyze() throws Exception {

        for (Class clazz : classes) {
            Class[] interfacess = clazz.getInterfaces();
            for (Class aInterface : interfacess) {
                boolean annotateWith = ClassScanner.isAnnotateWith(aInterface, Component.class);
                if (annotateWith) {
                    boolean containsKey = interfaceImplMap.containsKey(aInterface);
                    if (containsKey) {
                        throw new Exception("组件接口：" + aInterface.getName() + "存在多个实现类！请检查代码！");
                    } else {
                        interfaceImplMap.put(aInterface, clazz);
                    }
                }
            }

            boolean autowiredRequied = false;
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                boolean autowiredPresent = field.isAnnotationPresent(Autowired.class);
                if (autowiredPresent) {
                    autowiredRequied = true;
                    Class<?> fieldType = field.getType();
                    boolean containsKey = classRequiredClassesMap.containsKey(clazz);
                    if (containsKey) {
                        Set<Class> classes = classRequiredClassesMap.get(clazz);
                        boolean contains = classes.contains(fieldType);
                        if (!contains){
                            classes.add(fieldType);
                        }
                    } else {
                        Set<Class> set = new CopyOnWriteArraySet<>();
                        set.add(fieldType);
                        classRequiredClassesMap.put(clazz, set);
                    }
                }
            }

            if (!autowiredRequied) {
                target.add(clazz);
            }
        }
    }



    /**
     * 对所有的component进行分离
     * 分别存储类和接口
     */
    private void classSeparateInit() {
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

    private void instanceInterface(Class clazz) {
        interfaces.add(clazz);
    }

    private void refresh() {

    }

    private void instance(Class clazz) {
        classes.add(clazz);
    }


    public Set<Class> getInterfaces() {
        return interfaces;
    }

    @Override
    public Set<Class> getClasses() {
        return classes;
    }

    public Set<Class> getTarget() {
        return target;
    }

    public Map<Class, Set<Class>> getClassRequiredClassesMap() {
        return classRequiredClassesMap;
    }

    public Map<Class, Class> getInterfaceImplMap() {
        return interfaceImplMap;
    }
}
