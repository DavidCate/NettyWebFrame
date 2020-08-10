package com.aimango.robot.server.core.container;

public interface ExternInstance {
    <T> T instance(Class<T> tClass);
}
