package com.aimango.robot.server.core.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public abstract class AbstractBaseController implements Callable {
    private static final Logger logger= LoggerFactory.getLogger(AbstractBaseController.class);
    protected abstract Object doService();
    @Override
    public Object call() throws Exception {
        doService();
        return null;
    }
}
