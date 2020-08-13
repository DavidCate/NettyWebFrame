package com.aimango.robot.server.core.container;

import java.lang.reflect.Method;
import java.util.Map;

public interface RestHttpHandlerContainer extends HttpHandlerContainer {
    Method getRestMethodByUri(String uri);

    Class getRestExecutorClassByMethod(Method restMethod);

    Map<String,Integer> getRestUriPathParamIndexInfoMap(String uri);
}
