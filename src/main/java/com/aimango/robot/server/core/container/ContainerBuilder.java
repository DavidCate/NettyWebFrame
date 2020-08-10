package com.aimango.robot.server.core.container;


import com.aimango.robot.server.core.annotation.Controller;
import com.aimango.robot.server.core.component.ClassScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

public class ContainerBuilder implements Builder<Container>{
    private static final Logger logger= LoggerFactory.getLogger(ContainerBuilder.class);
    private String packageName;

    public ContainerBuilder(String basePackage) {
        this.packageName=basePackage;
    }

    @Override
    public Container build() throws Exception {
        logger.info("构建容器");
        //扫描所有@Component的类
        Set<Class> classes = ClassScanner.scanComponents(packageName);
        IocTontainer iocTontainer = new IocTontainer(classes);
        System.out.println("....");
        HttpClassContainer httpClassContainer=new HttpClassContainer(classes);
        return httpClassContainer;
    }
}
