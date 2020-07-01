package com.aimango.robot.server.core.container;

public interface Container {
    Class getClass(String beanName);
    Class addClass(String beanName,Class clazz);
    boolean hasClass(String beanName);
    boolean hasClass(Class clazz);

}
