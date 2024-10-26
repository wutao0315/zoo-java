package com.wxius.framework.zoo.mockserver;

import com.wxius.framework.zoo.build.ZooInjector;
import com.wxius.framework.zoo.core.ZooClientSystemConsts;
//import com.wxius.framework.zoo.core.dto.ZooConfig;
//import com.wxius.framework.zoo.core.dto.ZooConfigNotification;
import com.wxius.framework.zoo.core.utils.ResourceUtils;
//import com.wxius.framework.zoo.infrastructure.ConfigServiceLocator;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class ZooTestingServer implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedZoo.class);
//    private static final Type notificationType = new TypeToken<List<ZooConfigNotification>>() {
//    }.getType();

    private static Method CONFIG_SERVICE_LOCATOR_CLEAR;
//    private static ConfigServiceLocator CONFIG_SERVICE_LOCATOR;

    private static final Gson GSON = new Gson();
    private final Map<String, Map<String, String>> addedOrModifiedPropertiesOfNamespace = Maps.newConcurrentMap();
    private final Map<String, Set<String>> deletedKeysOfNamespace = Maps.newConcurrentMap();

    private MockWebServer server;

    private boolean started;

    private boolean closed;

    static {
//        try {
//            System.setProperty("zoo.longPollingInitialDelayInMills", "0");
//            CONFIG_SERVICE_LOCATOR = ZooInjector.getInstance(ConfigServiceLocator.class);
//            CONFIG_SERVICE_LOCATOR_CLEAR = ConfigServiceLocator.class.getDeclaredMethod("initConfigServices");
//            CONFIG_SERVICE_LOCATOR_CLEAR.setAccessible(true);
//        } catch (NoSuchMethodException e) {
//            logger.error(e.getMessage(), e);
//        }
    }

    public void start() throws IOException {
        clear();
        server = new MockWebServer();
        final Dispatcher dispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().startsWith("/notifications/v2")) {
//                    String notifications = request.getRequestUrl().queryParameter("notifications");
//                    return new MockResponse().setResponseCode(200).setBody(mockLongPollBody(notifications));
                }
                if (request.getPath().startsWith("/configs")) {
//                    List<String> pathSegments = request.getRequestUrl().pathSegments();
//                    // appId and cluster might be used in the future
//                    String appId = pathSegments.get(1);
//                    String cluster = pathSegments.get(2);
//                    String namespace = pathSegments.get(3);
//                    return new MockResponse().setResponseCode(200).setBody(loadConfigFor(namespace));
                }
                return new MockResponse().setResponseCode(404);
            }
        };

        server.setDispatcher(dispatcher);
        server.start();

        mockConfigServiceUrl("http://localhost:" + server.getPort());
        started = true;
    }

    public void close() {
        try {
            clear();
            server.close();
        } catch (Exception e) {
            logger.error("stop zoo server error", e);
        } finally {
            closed = true;
        }
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isStarted() {
        return started;
    }

    private void clear() {
        resetOverriddenProperties();
    }

    private void mockConfigServiceUrl(String url) {
        System.setProperty(ZooClientSystemConsts.ZOO_CONFIG_SERVICE, url);

//        try {
//            CONFIG_SERVICE_LOCATOR_CLEAR.invoke(CONFIG_SERVICE_LOCATOR);
//        } catch (Exception e) {
//            throw new IllegalStateException("Invoke config service locator clear failed.", e);
//        }
    }

//    private String loadConfigFor(String namespace) {
//        String filename = String.format("mockdata-%s.properties", namespace);
//        final Properties prop = ResourceUtils.readConfigFile(filename, new Properties());
//        Map<String, String> configurations = Maps.newHashMap();
//        for (String propertyName : prop.stringPropertyNames()) {
//            configurations.put(propertyName, prop.getProperty(propertyName));
//        }
//        ZooConfig zooConfig = new ZooConfig("someAppId", "someCluster", namespace, "someReleaseKey");
//
//        Map<String, String> mergedConfigurations = mergeOverriddenProperties(namespace, configurations);
//        zooConfig.setConfigurations(mergedConfigurations);
//        return GSON.toJson(zooConfig);
//    }

//    private String mockLongPollBody(String notificationsStr) {
//        List<ZooConfigNotification> oldNotifications = GSON.fromJson(notificationsStr, notificationType);
//        List<ZooConfigNotification> newNotifications = new ArrayList<>();
//        for (ZooConfigNotification notification : oldNotifications) {
//            newNotifications
//                    .add(new ZooConfigNotification(notification.getNamespaceName(), notification.getNotificationId() + 1));
//        }
//        return GSON.toJson(newNotifications);
//    }

    /**
     * 合并用户对namespace的修改
     */
    private Map<String, String> mergeOverriddenProperties(String namespace, Map<String, String> configurations) {
        if (addedOrModifiedPropertiesOfNamespace.containsKey(namespace)) {
            configurations.putAll(addedOrModifiedPropertiesOfNamespace.get(namespace));
        }
        if (deletedKeysOfNamespace.containsKey(namespace)) {
            for (String k : deletedKeysOfNamespace.get(namespace)) {
                configurations.remove(k);
            }
        }
        return configurations;
    }

    /**
     * Add new property or update existed property
     */
    public void addOrModifyProperty(String namespace, String someKey, String someValue) {
        if (addedOrModifiedPropertiesOfNamespace.containsKey(namespace)) {
            addedOrModifiedPropertiesOfNamespace.get(namespace).put(someKey, someValue);
        } else {
            Map<String, String> m = Maps.newConcurrentMap();
            m.put(someKey, someValue);
            addedOrModifiedPropertiesOfNamespace.put(namespace, m);
        }
    }

    /**
     * Delete existed property
     */
    public void deleteProperty(String namespace, String someKey) {
        if (deletedKeysOfNamespace.containsKey(namespace)) {
            deletedKeysOfNamespace.get(namespace).add(someKey);
        } else {
            Set<String> m = Sets.newConcurrentHashSet();
            m.add(someKey);
            deletedKeysOfNamespace.put(namespace, m);
        }
    }

    /**
     * reset overridden properties
     */
    public void resetOverriddenProperties() {
        addedOrModifiedPropertiesOfNamespace.clear();
        deletedKeysOfNamespace.clear();
    }
}
