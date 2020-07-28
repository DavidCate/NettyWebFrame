package com.aimango.robot.server.core.container;

public interface Container {
    /**
     * 通过全类名获取对象实例
     * @param beanName
     * @return
     */
    Class getClass(String beanName);
    Class addClass(String beanName,Class clazz);
    boolean hasClass(String beanName);
    boolean hasClass(Class clazz);

}
