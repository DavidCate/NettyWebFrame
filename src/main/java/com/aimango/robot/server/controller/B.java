package com.aimango.robot.server.controller;

import com.aimango.robot.server.core.annotation.Autowired;
import com.aimango.robot.server.core.annotation.Component;

@Component
public class B {
    @Autowired
    A a;
}
