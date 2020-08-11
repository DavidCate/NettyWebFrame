package com.aimango.robot.server.service.impl;

import com.aimango.robot.server.core.annotation.Component;
import com.aimango.robot.server.service.UserService;

@Component
public class UserServiceImpl implements UserService {
    @Override
    public String loginService() {
        return "登录服务";
    }
}
