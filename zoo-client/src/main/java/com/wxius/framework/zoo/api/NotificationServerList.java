package com.wxius.framework.zoo.api;

import java.util.ArrayList;
import java.util.List;

public class NotificationServerList {
    private List<String> nodes = new ArrayList<>();
    private String md5;

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
