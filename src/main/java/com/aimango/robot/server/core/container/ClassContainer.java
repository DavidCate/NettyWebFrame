package com.aimango.robot.server.core.container;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class ClassContainer implements Container {
    private Map<String,Class> classes=new ConcurrentHashMap<String,Class>();

    public ClassContainer(Set<Class> classes) {
        init(classes);
    }

    private void init(Set<Class> classes) {
        for (Class clazz:classes){
            String name = clazz.getName();
            this.classes.put(name,clazz);
        }
    }

    public Set<Class> getClasses(){
        Collection<Class> classes = this.classes.values();
        Set set=new CopyOnWriteArraySet(classes);
        return set;
    }


    @Override
    public Class getClass(String beanName) {
        return null;
    }

    @Override
    public Class addClass(String beanName, Class clazz) {
        return null;
    }

    @Override
    public boolean hasClass(String beanName) {
        return false;
    }

    @Override
    public boolean hasClass(Class clazz) {
        return false;
    }
}
