package com.wxius.framework.zoo.api;

import java.util.Map;

public class NotificationMessage extends Notification {
    private Map<String, String> configurations;

    public Map<String, String> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(Map<String, String> configurations) {
        this.configurations = configurations;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NotificationMessage{");
        sb.append("name='").append(name).append('\'');
        sb.append(", configurations='").append(configurations).append('\'');
        sb.append(", md5='").append(md5).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
