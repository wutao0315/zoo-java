package com.wxius.framework.zoo.util;

import static com.wxius.framework.zoo.util.factory.PropertiesFactory.ZOO_PROPERTY_ORDER_ENABLE;

import com.wxius.framework.zoo.Constants;
import com.wxius.framework.zoo.ZooPublishOption;
import com.wxius.framework.zoo.ZooSubscriptionOption;
import com.wxius.framework.zoo.core.ZooClientSystemConsts;
import com.wxius.framework.zoo.core.ConfigConsts;
import com.wxius.framework.zoo.core.MetaDomainConsts;
import com.wxius.framework.zoo.core.enums.Env;
import com.wxius.framework.zoo.core.enums.EnvUtils;
import com.wxius.framework.zoo.core.utils.DeprecatedPropertyNotifyUtil;
import com.wxius.framework.foundation.Foundation;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.RateLimiter;
import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigUtil {

  private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);
  private int refreshInterval = 5;
  private TimeUnit refreshIntervalTimeUnit = TimeUnit.MINUTES;
  private int connectTimeout = 1000; //1 second
  private int readTimeout = 5000; //5 seconds
  private String cluster;

  private  String encryptKey = "Zoo@Zop";
  private int loadConfigQPS = 2; //2 times per second
  private int longPollQPS = 2; //2 times per second
  //for on error retry
  private long onErrorRetryInterval = 1;//1 second
  private TimeUnit onErrorRetryIntervalTimeUnit = TimeUnit.SECONDS;//1 second
  //for typed config cache of parser result, e.g. integer, double, long, etc.
  private long maxConfigCacheSize = 500;//500 cache key
  private long configCacheExpireTime = 1;//1 minute
  private TimeUnit configCacheExpireTimeUnit = TimeUnit.MINUTES;//1 minute
  private long longPollingInitialDelayInMills = 2000;//2 seconds
  private boolean autoUpdateInjectedSpringProperties = true;
  private final RateLimiter warnLogRateLimiter;
  private boolean propertiesOrdered = false;
  private boolean propertyNamesCacheEnabled = false;
//  private boolean propertyFileCacheEnabled = true;
  private boolean overrideSystemProperties = true;
  private boolean encryptEnable = false;










  private boolean enable = true;
  private String scheme  = "http";
  private List<String> serverAddresses = new ArrayList<>();
  // ms
  private int defaultTimeout = 1000;

  // 实例唯一标识
  private String clientId = UUID.randomUUID().toString();

  private int reciveInterval= 1000;
  private String clientName  = "";
  private boolean secure = false;
  private String accessKey  = "";
  private String secretKey  = "";
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

















  public ConfigUtil() {
    warnLogRateLimiter = RateLimiter.create(0.017); // 1 warning log output per minute
    initRefreshInterval();
    initConnectTimeout();
    initReadTimeout();
    initCluster();
    initEncryptKey();
    initQPS();
    initMaxConfigCacheSize();
    initLongPollingInitialDelayInMills();
    initAutoUpdateInjectedSpringProperties();
    initPropertiesOrdered();
    initPropertyNamesCacheEnabled();
//    initPropertyFileCacheEnabled();
    initOverrideSystemProperties();
    initEncryptEnable();
  }

  /**
   * Get the app id for the current application.
   *
   * @return the app id or ConfigConsts.NO_APPID_PLACEHOLDER if app id is not available
   */
  public String getAppId() {
    String appId = Foundation.app().getAppId();
    if (Strings.isNullOrEmpty(appId)) {
      appId = ConfigConsts.NO_APPID_PLACEHOLDER;
      if (warnLogRateLimiter.tryAcquire()) {
        logger.warn(
            "app.id is not set, please make sure it is set in classpath:/META-INF/app.properties, now zoo will only load public namespace configurations!");
      }
    }
    return appId;
  }

  /**
   * Get the zoo label for the current application.
   *
   * @return zoo Label
   */
  public String getZooLabel() {
    return Foundation.app().getZooLabel();
  }

  /**
   * Get the access key secret for the current application.
   *
   * @return the current access key secret, null if there is no such secret.
   */
  public String getAccessKeySecret() {
    return Foundation.app().getAccessKeySecret();
  }

  /**
   * Get the data center info for the current application.
   *
   * @return the current data center, null if there is no such info.
   */
  public String getDataCenter() {
    return Foundation.server().getDataCenter();
  }

  private void initCluster() {
    //Load data center from system property
    cluster = System.getProperty(ConfigConsts.ZOO_CLUSTER_KEY);

    //Use data center as cluster
    if (Strings.isNullOrEmpty(cluster)) {
      cluster = getDataCenter();
    }

    //Use default cluster
    if (Strings.isNullOrEmpty(cluster)) {
      cluster = ConfigConsts.CLUSTER_NAME_DEFAULT;
    }
  }

  private void initEncryptKey() {
    //Load data center from system property
    encryptKey = System.getProperty(ConfigConsts.ZOO_ENCRYPT_KEY);

    //Use default key
    if (Strings.isNullOrEmpty(encryptKey)) {
      encryptKey = ConfigConsts.ENCRYPT_DEFAULT;
    }
  }



  /**
   * Get the cluster name for the current application.
   *
   * @return the cluster name, or "default" if not specified
   */
  public String getCluster() {
    return cluster;
  }

  public String getEncryptKey(){return encryptKey;}

  /**
   * Get the current environment.
   *
   * @return the env, UNKNOWN if env is not set or invalid
   */
  public Env getZooEnv() {
    return EnvUtils.transformEnv(Foundation.server().getEnvType());
  }

  public String getLocalIp() {
    return Foundation.net().getHostAddress();
  }

  public String getMetaServerDomainName() {
    return MetaDomainConsts.getDomain(getZooEnv());
  }

  private void initConnectTimeout() {
    String customizedConnectTimeout = System.getProperty("zoo.connectTimeout");
    if (!Strings.isNullOrEmpty(customizedConnectTimeout)) {
      try {
        connectTimeout = Integer.parseInt(customizedConnectTimeout);
      } catch (Throwable ex) {
        logger.error("Config for zoo.connectTimeout is invalid: {}", customizedConnectTimeout);
      }
    }
  }

  public int getConnectTimeout() {
    return connectTimeout;
  }

  private void initReadTimeout() {
    String customizedReadTimeout = System.getProperty("zoo.readTimeout");
    if (!Strings.isNullOrEmpty(customizedReadTimeout)) {
      try {
        readTimeout = Integer.parseInt(customizedReadTimeout);
      } catch (Throwable ex) {
        logger.error("Config for zoo.readTimeout is invalid: {}", customizedReadTimeout);
      }
    }
  }

  public int getReadTimeout() {
    return readTimeout;
  }

  private void initRefreshInterval() {
    String customizedRefreshInterval = System.getProperty("zoo.refreshInterval");
    if (!Strings.isNullOrEmpty(customizedRefreshInterval)) {
      try {
        refreshInterval = Integer.parseInt(customizedRefreshInterval);
      } catch (Throwable ex) {
        logger.error("Config for zoo.refreshInterval is invalid: {}", customizedRefreshInterval);
      }
    }
  }

  public int getRefreshInterval() {
    return refreshInterval;
  }

  public TimeUnit getRefreshIntervalTimeUnit() {
    return refreshIntervalTimeUnit;
  }

  private void initQPS() {
    String customizedLoadConfigQPS = System.getProperty("zoo.loadConfigQPS");
    if (!Strings.isNullOrEmpty(customizedLoadConfigQPS)) {
      try {
        loadConfigQPS = Integer.parseInt(customizedLoadConfigQPS);
      } catch (Throwable ex) {
        logger.error("Config for zoo.loadConfigQPS is invalid: {}", customizedLoadConfigQPS);
      }
    }

    String customizedLongPollQPS = System.getProperty("zoo.longPollQPS");
    if (!Strings.isNullOrEmpty(customizedLongPollQPS)) {
      try {
        longPollQPS = Integer.parseInt(customizedLongPollQPS);
      } catch (Throwable ex) {
        logger.error("Config for zoo.longPollQPS is invalid: {}", customizedLongPollQPS);
      }
    }
  }

  public int getLoadConfigQPS() {
    return loadConfigQPS;
  }

  public int getLongPollQPS() {
    return longPollQPS;
  }

  public long getOnErrorRetryInterval() {
    return onErrorRetryInterval;
  }

  public TimeUnit getOnErrorRetryIntervalTimeUnit() {
    return onErrorRetryIntervalTimeUnit;
  }

  public String getDefaultLocalCacheDir() {
    String cacheRoot = getCustomizedCacheRoot();

    if (!Strings.isNullOrEmpty(cacheRoot)) {
      return cacheRoot + File.separator + getAppId();
    }

    cacheRoot = isOSWindows() ? "C:\\opt\\data\\%s" : "/opt/data/%s";
    return String.format(cacheRoot, getAppId());
  }

  private String getCustomizedCacheRoot() {
    // 1. Get from System Property
    String cacheRoot = System.getProperty(ZooClientSystemConsts.ZOO_CACHE_DIR);
    if (Strings.isNullOrEmpty(cacheRoot)) {
      // 2. Get from OS environment variable
      cacheRoot = System.getenv(ZooClientSystemConsts.ZOO_CACHE_DIR_ENVIRONMENT_VARIABLES);
    }
    if (Strings.isNullOrEmpty(cacheRoot)) {
      // 3. Get from server.properties
      cacheRoot = Foundation.server().getProperty(ZooClientSystemConsts.ZOO_CACHE_DIR, null);
    }
    if (Strings.isNullOrEmpty(cacheRoot)) {
      // 4. Get from app.properties
      cacheRoot = Foundation.app().getProperty(ZooClientSystemConsts.ZOO_CACHE_DIR, null);
    }
    if (Strings.isNullOrEmpty(cacheRoot)) {
      // 5. Get from deprecated config
      cacheRoot = getDeprecatedCustomizedCacheRoot();
    }
    return cacheRoot;
  }

  @SuppressWarnings("deprecation")
  private String getDeprecatedCustomizedCacheRoot() {
    // 1. Get from System Property
    String cacheRoot = System.getProperty(ZooClientSystemConsts.DEPRECATED_ZOO_CACHE_DIR);
    if (!Strings.isNullOrEmpty(cacheRoot)) {
      DeprecatedPropertyNotifyUtil.warn(ZooClientSystemConsts.DEPRECATED_ZOO_CACHE_DIR,
          ZooClientSystemConsts.ZOO_CACHE_DIR);
    }
    if (Strings.isNullOrEmpty(cacheRoot)) {
      // 2. Get from OS environment variable
      cacheRoot = System.getenv(ZooClientSystemConsts.DEPRECATED_ZOO_CACHE_DIR_ENVIRONMENT_VARIABLES);
      if (!Strings.isNullOrEmpty(cacheRoot)) {
        DeprecatedPropertyNotifyUtil
            .warn(ZooClientSystemConsts.DEPRECATED_ZOO_CACHE_DIR_ENVIRONMENT_VARIABLES,
                ZooClientSystemConsts.ZOO_CACHE_DIR_ENVIRONMENT_VARIABLES);
      }
    }
    if (Strings.isNullOrEmpty(cacheRoot)) {
      // 3. Get from server.properties
      cacheRoot = Foundation.server().getProperty(ZooClientSystemConsts.DEPRECATED_ZOO_CACHE_DIR, null);
      if (!Strings.isNullOrEmpty(cacheRoot)) {
        DeprecatedPropertyNotifyUtil.warn(ZooClientSystemConsts.DEPRECATED_ZOO_CACHE_DIR,
            ZooClientSystemConsts.ZOO_CACHE_DIR);
      }
    }
    if (Strings.isNullOrEmpty(cacheRoot)) {
      // 4. Get from app.properties
      cacheRoot = Foundation.app().getProperty(ZooClientSystemConsts.DEPRECATED_ZOO_CACHE_DIR, null);
      if (!Strings.isNullOrEmpty(cacheRoot)) {
        DeprecatedPropertyNotifyUtil.warn(ZooClientSystemConsts.DEPRECATED_ZOO_CACHE_DIR,
            ZooClientSystemConsts.ZOO_CACHE_DIR);
      }
    }
    return cacheRoot;
  }

//  public boolean isInLocalMode() {
//    try {
//      return Env.LOCAL == getZooEnv();
//    } catch (Throwable ex) {
//      //ignore
//    }
//    return false;
//  }

  public boolean isOSWindows() {
    String osName = System.getProperty("os.name");
    if (Strings.isNullOrEmpty(osName)) {
      return false;
    }
    return osName.startsWith("Windows");
  }

  private void initMaxConfigCacheSize() {
    String customizedConfigCacheSize = System.getProperty("zoo.configCacheSize");
    if (!Strings.isNullOrEmpty(customizedConfigCacheSize)) {
      try {
        maxConfigCacheSize = Long.parseLong(customizedConfigCacheSize);
      } catch (Throwable ex) {
        logger.error("Config for zoo.configCacheSize is invalid: {}", customizedConfigCacheSize);
      }
    }
  }

  public long getMaxConfigCacheSize() {
    return maxConfigCacheSize;
  }

  public long getConfigCacheExpireTime() {
    return configCacheExpireTime;
  }

  public TimeUnit getConfigCacheExpireTimeUnit() {
    return configCacheExpireTimeUnit;
  }

  private void initLongPollingInitialDelayInMills() {
    String customizedLongPollingInitialDelay = System
        .getProperty("zoo.longPollingInitialDelayInMills");
    if (!Strings.isNullOrEmpty(customizedLongPollingInitialDelay)) {
      try {
        longPollingInitialDelayInMills = Long.parseLong(customizedLongPollingInitialDelay);
      } catch (Throwable ex) {
        logger.error("Config for zoo.longPollingInitialDelayInMills is invalid: {}",
            customizedLongPollingInitialDelay);
      }
    }
  }

  public long getLongPollingInitialDelayInMills() {
    return longPollingInitialDelayInMills;
  }

  private void initAutoUpdateInjectedSpringProperties() {
    // 1. Get from System Property
    String enableAutoUpdate = System.getProperty("zoo.autoUpdateInjectedSpringProperties");
    if (Strings.isNullOrEmpty(enableAutoUpdate)) {
      // 2. Get from app.properties
      enableAutoUpdate = Foundation.app()
          .getProperty("zoo.autoUpdateInjectedSpringProperties", null);
    }
    if (!Strings.isNullOrEmpty(enableAutoUpdate)) {
      autoUpdateInjectedSpringProperties = Boolean.parseBoolean(enableAutoUpdate.trim());
    }
  }

  public boolean isAutoUpdateInjectedSpringPropertiesEnabled() {
    return autoUpdateInjectedSpringProperties;
  }

  private void initPropertiesOrdered() {
    String enablePropertiesOrdered = System.getProperty(ZOO_PROPERTY_ORDER_ENABLE);

    if (Strings.isNullOrEmpty(enablePropertiesOrdered)) {
      enablePropertiesOrdered = Foundation.app().getProperty(ZOO_PROPERTY_ORDER_ENABLE, "false");
    }

    if (!Strings.isNullOrEmpty(enablePropertiesOrdered)) {
      try {
        propertiesOrdered = Boolean.parseBoolean(enablePropertiesOrdered);
      } catch (Throwable ex) {
        logger.warn("Config for {} is invalid: {}, set default value: false",
            ZOO_PROPERTY_ORDER_ENABLE, enablePropertiesOrdered);
      }
    }
  }

  public boolean isPropertiesOrderEnabled() {
    return propertiesOrdered;
  }

  public boolean isPropertyNamesCacheEnabled() {
    return propertyNamesCacheEnabled;
  }

//  public boolean isPropertyFileCacheEnabled() {
//    return propertyFileCacheEnabled;
//  }

  public boolean isOverrideSystemProperties() {
    return overrideSystemProperties;
  }

  public boolean isEncryptEnable() {
    return encryptEnable;
  }

  private void initPropertyNamesCacheEnabled() {
    propertyNamesCacheEnabled = getPropertyBoolean(ZooClientSystemConsts.ZOO_PROPERTY_NAMES_CACHE_ENABLE,
            ZooClientSystemConsts.ZOO_PROPERTY_NAMES_CACHE_ENABLE_ENVIRONMENT_VARIABLES,
            propertyNamesCacheEnabled);
  }

//  private void initPropertyFileCacheEnabled() {
//    propertyFileCacheEnabled = getPropertyBoolean(ZooClientSystemConsts.ZOO_CACHE_FILE_ENABLE,
//            ZooClientSystemConsts.ZOO_CACHE_FILE_ENABLE_ENVIRONMENT_VARIABLES,
//            propertyFileCacheEnabled);
//  }

  private void initOverrideSystemProperties() {
    overrideSystemProperties = getPropertyBoolean(ZooClientSystemConsts.ZOO_OVERRIDE_SYSTEM_PROPERTIES,
            ZooClientSystemConsts.ZOO_OVERRIDE_SYSTEM_PROPERTIES,
            overrideSystemProperties);
  }

  private void initEncryptEnable() {
    encryptEnable = getPropertyBoolean(ZooClientSystemConsts.ZOO_ENCRYPT_ENABLE,
            ZooClientSystemConsts.ZOO_ENCRYPT_ENABLE,
            encryptEnable);
  }


  private boolean getPropertyBoolean(String propertyName, String envName, boolean defaultVal) {
    String enablePropertyNamesCache = System.getProperty(propertyName);
    if (Strings.isNullOrEmpty(enablePropertyNamesCache)) {
      enablePropertyNamesCache = System.getenv(envName);
    }
    if (Strings.isNullOrEmpty(enablePropertyNamesCache)) {
      enablePropertyNamesCache = Foundation.app().getProperty(propertyName, null);
    }
    if (!Strings.isNullOrEmpty(enablePropertyNamesCache)) {
      try {
        return Boolean.parseBoolean(enablePropertyNamesCache);
      } catch (Throwable ex) {
        logger.warn("Config for {} is invalid: {}, set default value: {}",
                propertyName, enablePropertyNamesCache, defaultVal);
      }
    }
    return defaultVal;
  }
}
