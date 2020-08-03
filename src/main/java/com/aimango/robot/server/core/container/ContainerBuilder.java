package com.aimango.robot.server.core.container;

import com.aimango.robot.server.core.annotation.Component;
import com.aimango.robot.server.core.annotation.Controller;
import com.aimango.robot.server.core.scanner.ClassScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ContainerBuilder implements Builder<Container>{
    private static final String PACKAGE="com.aimango.robot.server";
    private static final Logger logger= LoggerFactory.getLogger(ContainerBuilder.class);

    @Override
    public Container build() throws InstantiationException, IllegalAccessException, IOException, ClassNotFoundException {
        logger.info("构建容器");
        //扫描所有@Component的类
        Set<Class> classSet = ClassScanner.scan(PACKAGE);
        Set<Class> classes=new HashSet<>();
        for (Class clazz:classSet){
            boolean annotateWith = ClassScanner.isAnnotateWith(clazz, Component.class);
            if (annotateWith){
                classes.add(clazz);
            }
        }


        FullClassContainer fullClassContainer=new FullClassContainer(classes);
        return fullClassContainer;
    }

    private void classDispacher(Class clazz){
        boolean annotationPresent = clazz.isAnnotationPresent(Controller.class);
        if (annotationPresent){

        }
    }
}
