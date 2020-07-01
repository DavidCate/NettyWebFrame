package com.aimango.robot.server.core.scanner;

import com.aimango.robot.server.core.annotation.Component;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ClassScanner {

    public static Set<Class> scanComponents() {
        Set<Class> classSet=new HashSet<>();
        String classpath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        System.setProperty("java.class.path",classpath);
        Reflections reflections=new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forJavaClassPath()));

        Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(Component.class);

        Iterator<Class<?>> iterator = typesAnnotatedWith.iterator();
        while (iterator.hasNext()){
            Class next = iterator.next();
            if (!(next.isAnnotation()||next.isEnum()||next.isInterface())){
                classSet.add(next);
            }
        }
        if (classSet.size()<=0){
            return null;
        }
        return classSet;
    }
}
