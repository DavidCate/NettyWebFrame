package com.aimango.robot.server;

import com.aimango.robot.server.core.launcher.HttpServerLauncher;

public class MyServer {
    public static void main(String[] args) {
        HttpServerLauncher.run(MyServer.class,args);
    }
}
