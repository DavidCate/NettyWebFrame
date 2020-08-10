package com.aimango.robot.server.core.annotation;

import com.aimango.robot.server.core.annotation.Component;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Repository {
    String value() default "mybatis";
}
