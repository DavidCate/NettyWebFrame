package com.aimango.robot.server.core.annotation.rest;

import com.aimango.robot.server.core.annotation.Component;
import com.aimango.robot.server.core.annotation.RequestMapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RestRequestMapping {
    public String url();
    public String method() default RequestMapping.Method.GET;

    class Method{
        public static final String GET="get";
        public static final String POST="post";
    }
}
