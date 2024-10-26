package com.wxius.framework.zoo;

import java.util.*;

public class ZooOptions {
    private boolean enable = true;
    private String scheme  = "http";
    private List<String> serverAddresses = new ArrayList<>();
    // ms
    private int defaultTimeout = 1000;

    // 实例唯一标识
    private String clientId = UUID.randomUUID().toString();

    // ms
    private int refreshInterval = 5000;
    private int reciveInterval= 1000;
    private String clientName  = "";
    private boolean secure = false;
    private String accessKey  = "";
    private String secretKey  = "";
    private boolean encryptEnable  = false;
    private String encryptKey = "Zoo@Zop";
    // meta info
    private Map<String, String> metadata = new HashMap<>();

    private String namespaceId = Constants.DefaultNamespaceId;
    // Get the group name info for the current application.
    private String groupName = Constants.DefaultGroupName;
    // regist
    private List<ZooPublishOption> publish = new ArrayList<>();
    // subscribe info
    private List<ZooSubscriptionOption> subscription = new ArrayList<>();
    private String localCacheDir = Constants.DefaultLocalCacheDir;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public List<String> getServerAddresses() {
        return serverAddresses;
    }

    public void setServerAddresses(List<String> serverAddresses) {
        this.serverAddresses = serverAddresses;
    }

    public int getDefaultTimeout() {
        return defaultTimeout;
    }

    public void setDefaultTimeout(int defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public int getReciveInterval() {
        return reciveInterval;
    }

    public void setReciveInterval(int reciveInterval) {
        this.reciveInterval = reciveInterval;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public boolean isEncryptEnable() {
        return encryptEnable;
    }

    public void setEncryptEnable(boolean encryptEnable) {
        this.encryptEnable = encryptEnable;
    }

    public String getEncryptKey() {
        return encryptKey;
    }

    public void setEncryptKey(String encryptKey) {
        this.encryptKey = encryptKey;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
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

    public List<ZooPublishOption> getPublish() {
        return publish;
    }

    public void setPublish(List<ZooPublishOption> publish) {
        this.publish = publish;
    }

    public List<ZooSubscriptionOption> getSubscription() {
        return subscription;
    }

    public void setSubscription(List<ZooSubscriptionOption> subscription) {
        this.subscription = subscription;
    }

    public String getLocalCacheDir() {
        return localCacheDir;
    }

    public void setLocalCacheDir(String localCacheDir) {
        this.localCacheDir = localCacheDir;
    }
}


