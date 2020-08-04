package com.aimango.robot.server.core.handler;

public class HeartBeatResponse {
    private int heartbeat=1;

    public int getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }
}
