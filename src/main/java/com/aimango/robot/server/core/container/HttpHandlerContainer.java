package com.aimango.robot.server.core.container;

import java.lang.reflect.Method;

public interface HttpHandlerContainer {
    Method getMethodByUri(String uri);
    Method addMethodForUri(String uri, Class clazz);
    Class getExecutorClassByMethod(Method method);
}
