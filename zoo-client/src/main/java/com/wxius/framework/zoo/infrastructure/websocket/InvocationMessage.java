package com.wxius.framework.zoo.infrastructure.websocket;

public class InvocationMessage {
    private String method = "";
    private String data = "";

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
