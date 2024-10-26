package com.wxius.framework.zoo;

public class ZooPublishOption
{

    private String serviceName = "";
    private String namespaceId = "";
    private String groupName= "";
    private String host = "";
    private int port;
    private String metadata= "";
    private String language = "json";


    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(namespaceId);
        sb.append("$$");
        sb.append(host).append(':').append(port).append('+');
        sb.append(groupName).append("@@");
        sb.append(serviceName).append("##");
        sb.append(metadata).append("&&").append(language);
        return sb.toString();
    }
}
