package com.aimango.robot.server.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
    public String url();
    public String method() default Method.GET;

    class Method{
        public static final String GET="get";
        public static final String POST="post";
    }

}

