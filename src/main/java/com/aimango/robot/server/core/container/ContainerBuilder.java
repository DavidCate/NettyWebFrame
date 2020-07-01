package com.aimango.robot.server.core.container;

import com.aimango.robot.server.core.annotation.Controller;
import com.aimango.robot.server.core.scanner.ClassScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class ContainerBuilder implements Builder<Container>{
    private static final Logger logger= LoggerFactory.getLogger(ContainerBuilder.class);

    @Override
    public Container build() throws InstantiationException, IllegalAccessException {
        logger.info("构建容器");
        //扫描所有@Component的类
        Set<Class> classes = ClassScanner.scanComponents();
        FullClassContainer fullClassContainer=new FullClassContainer(classes);
        return fullClassContainer;
    }

    private void classDispacher(Class clazz){
        boolean annotationPresent = clazz.isAnnotationPresent(Controller.class);
        if (annotationPresent){

        }
    }
}
