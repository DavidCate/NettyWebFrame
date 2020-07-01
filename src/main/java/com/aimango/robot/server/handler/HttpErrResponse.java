package com.aimango.robot.server.handler;

public class HttpErrResponse {
    private String msg;
    private int statusCode=400;
    private String status;

    public HttpErrResponse() {
    }

    public HttpErrResponse(String msg) {
        this.msg=msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
