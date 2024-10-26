package com.wxius.framework.zoo.infrastructure;

import com.wxius.framework.zoo.Zoo;
import com.wxius.framework.zoo.ZooOptions;
import com.wxius.framework.zoo.api.Notification;
import com.wxius.framework.zoo.api.NotificationMessage;
import com.wxius.framework.zoo.build.ZooInjector;
import com.wxius.framework.zoo.core.ConfigConsts;
//import com.wxius.framework.zoo.core.dto.ZooConfig;
//import com.wxius.framework.zoo.core.dto.ZooNotificationMessages;
//import com.wxius.framework.zoo.core.dto.ServiceDTO;
import com.wxius.framework.zoo.core.schedule.ExponentialSchedulePolicy;
import com.wxius.framework.zoo.core.schedule.SchedulePolicy;
import com.wxius.framework.zoo.core.signature.Signature;
import com.wxius.framework.zoo.core.utils.ZooThreadFactory;
import com.wxius.framework.zoo.core.utils.DeferredLoggerFactory;
import com.wxius.framework.zoo.core.utils.StringUtils;
import com.wxius.framework.zoo.exceptions.ZooException;
import com.wxius.framework.zoo.exceptions.ZooStatusCodeException;
import com.wxius.framework.zoo.tracer.Tracer;
import com.wxius.framework.zoo.tracer.spi.Transaction;
import com.wxius.framework.zoo.util.ConfigUtil;
import com.wxius.framework.zoo.util.ExceptionUtil;
import com.wxius.framework.zoo.util.http.HttpRequest;
import com.wxius.framework.zoo.util.http.HttpResponse;
import com.wxius.framework.zoo.util.http.HttpClient;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.google.common.util.concurrent.RateLimiter;
import com.google.gson.Gson;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;

public class HttpRepository extends AbstractRepository {
  private static final Logger logger = DeferredLoggerFactory.getLogger(HttpRepository.class);
  private static final Joiner STRING_JOINER = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR);
  private static final Joiner.MapJoiner MAP_JOINER = Joiner.on("&").withKeyValueSeparator("=");
  private static final Escaper pathEscaper = UrlEscapers.urlPathSegmentEscaper();
  private static final Escaper queryParamEscaper = UrlEscapers.urlFormParameterEscaper();

//  private final ConfigServiceLocator m_configServiceLocator;
  private final HttpClient m_httpClient;
  private final ConfigUtil m_configUtil;
//  private final RemoteConfigLongPollService remoteConfigLongPollService;
//  private volatile AtomicReference<ZooConfig> m_configCache;
  private final String m_name;
  private final static ScheduledExecutorService m_executorService;
//  private final AtomicReference<ServiceDTO> m_longPollServiceDto;
//  private final AtomicReference<ZooNotificationMessages> m_remoteMessages;
  private final RateLimiter m_loadConfigRateLimiter;
  private final AtomicBoolean m_configNeedForceRefresh;
  private final SchedulePolicy m_loadConfigFailSchedulePolicy;
  private static final Gson GSON = new Gson();
  private final ServerListLocator m_serviceLocator;
  private final AtomicReference<Notification> m_remoteCache;
  private final AtomicReference<NotificationMessage> m_cache;
  private final RateLimiter m_loadDatumRateLimiter;
  private final AtomicBoolean m_datumNeedForceRefresh;
  private final SchedulePolicy m_loadDatumFailSchedulePolicy;

  private final ZooOptions m_options;

  private final WebSocketService m_remoteService;

  static {
    m_executorService = Executors.newScheduledThreadPool(1,
        ZooThreadFactory.create("HttpRepository", true));
  }

  /**
   * Constructor.
   *
   * @param name the name
   */
  public HttpRepository(String name) {
    m_name = name;
//    m_configCache = new AtomicReference<>();
    m_configUtil = ZooInjector.getInstance(ConfigUtil.class);
    m_httpClient = ZooInjector.getInstance(HttpClient.class);

//    m_configServiceLocator = ZooInjector.getInstance(ConfigServiceLocator.class);
//    remoteConfigLongPollService = ZooInjector.getInstance(RemoteConfigLongPollService.class);
//    m_longPollServiceDto = new AtomicReference<>();
//    m_remoteMessages = new AtomicReference<>();

    m_remoteCache = new AtomicReference<>();
    m_cache = new AtomicReference<>();
    m_loadDatumRateLimiter = RateLimiter.create(m_configUtil.getLoadConfigQPS());
    m_datumNeedForceRefresh = new AtomicBoolean(true);

    m_options = ZooInjector.getInstance(ZooOptions.class);
    m_serviceLocator = ZooInjector.getInstance(ServerListLocator.class);
    m_loadDatumFailSchedulePolicy = new ExponentialSchedulePolicy(m_configUtil.getOnErrorRetryInterval(),
            m_configUtil.getOnErrorRetryInterval() * 8);

    m_remoteService = ZooInjector.getInstance(WebSocketService.class);

    m_loadConfigRateLimiter = RateLimiter.create(m_configUtil.getLoadConfigQPS());
    m_configNeedForceRefresh = new AtomicBoolean(true);
    m_loadConfigFailSchedulePolicy = new ExponentialSchedulePolicy(m_configUtil.getOnErrorRetryInterval(),
        m_configUtil.getOnErrorRetryInterval() * 8);

    this.trySync();
    schedulePeriodicRefresh();
    m_remoteService.submit(m_name, this);
  }

  @Override
  public Properties getDatum() {
    if (m_cache.get() == null) {
      this.sync();
    }
    return transformMapToProperties(m_cache.get().getConfigurations());
  }

  private void schedulePeriodicRefresh() {
    logger.debug("Schedule periodic refresh with interval: {} {}",
            m_configUtil.getRefreshInterval(), m_configUtil.getRefreshIntervalTimeUnit());

    m_executorService.scheduleAtFixedRate(
            new Runnable() {
              @Override
              public void run() {
                Tracer.logEvent("Zoo.ConfigService", String.format("periodicRefresh: %s", m_name));
                logger.debug("refresh config for namespace: {}", m_name);
                trySync();
                Tracer.logEvent("Zoo.Client.Version", Zoo.VERSION);
              }
            }, m_configUtil.getRefreshInterval(), m_configUtil.getRefreshInterval(),
            m_configUtil.getRefreshIntervalTimeUnit());
  }

  public NotificationMessage getCache()
  {
    return m_cache.get();
  }

  private Properties transformMapToProperties(Map<String, String> map) {
    Properties result = propertiesFactory.getPropertiesInstance();
    result.putAll(map);
    return result;
  }

  @Override
  protected synchronized void sync() {
    Transaction transaction = Tracer.newTransaction("Zoo.DatumService", "syncRemoteConfig");

    try {

      NotificationMessage previous = m_cache.get();
      NotificationMessage current = loadRemote();

      //reference equals means HTTP 304
      if (previous != current) {
        logger.debug("Remote Config refreshed!");
        m_cache.set(current);
        this.fireRepositoryChange(m_name, this.getDatum());
      }

      if (current != null) {
        Tracer.logEvent(String.format("Zoo.Client.Datum.%s", current.getName()),
                current.getMd5());
      }

      transaction.setStatus(Transaction.SUCCESS);
    } catch (Throwable ex) {
      transaction.setStatus(ex);
      throw ex;
    } finally {
      transaction.complete();
    }
  }

  private NotificationMessage loadRemote() {
    if (!m_loadDatumRateLimiter.tryAcquire(5, TimeUnit.SECONDS)) {
      //wait at most 5 seconds
      try {
        TimeUnit.SECONDS.sleep(5);
      } catch (InterruptedException e) {
      }
    }

    String appId = m_configUtil.getAppId();
    String cluster = m_configUtil.getCluster();
    String secret = m_configUtil.getAccessKeySecret();

    Tracer.logEvent("Zoo.Client.DatumMeta", STRING_JOINER.join(appId, cluster, m_name));
    int maxRetries = m_datumNeedForceRefresh.get() ? 2 : 1;
    long onErrorSleepTime = 0; // 0 means no sleep
    Throwable exception = null;
    boolean notFound = false;
    String url = null;
    for (int i = 0; i < maxRetries; i++) {
      if (onErrorSleepTime > 0) {
        logger.warn(
                "Load config failed, will retry in {} {}. appId: {}, cluster: {}, namespaces: {}",
                onErrorSleepTime, m_configUtil.getOnErrorRetryIntervalTimeUnit(), appId, cluster, m_name);

        try {
          m_configUtil.getOnErrorRetryIntervalTimeUnit().sleep(onErrorSleepTime);
        } catch (InterruptedException e) {
          //ignore
        }
      }

      url = assembleQueryDatumUrl(m_serviceLocator.getCurrentServer(), m_remoteCache.get(), m_cache.get());
      logger.debug("Loading config from {}", url);
      HttpRequest request = new HttpRequest(url);
      if (!StringUtils.isBlank(secret)) {
        Map<String, String> headers = Signature.buildHttpHeaders(url, appId, secret);
        request.setHeaders(headers);
      }

      Transaction transaction = Tracer.newTransaction("Zoo.DatumService", "queryConfig");
      transaction.addData("Url", url);
      try {
        HttpResponse<NotificationMessage> response = m_httpClient.doGet(request, NotificationMessage.class);
        m_datumNeedForceRefresh.set(false);
        m_loadDatumFailSchedulePolicy.success();

        transaction.addData("StatusCode", response.getStatusCode());
        transaction.setStatus(Transaction.SUCCESS);

        if (response.getStatusCode() == 304) {
          logger.debug("Config server responds with 304 HTTP status code.");

          return m_cache.get();
        }

        NotificationMessage result = response.getBody();

        logger.debug("Loaded datum for {}: {}", m_name, result);

        return result;
      } catch (ZooStatusCodeException ex) {
        ZooStatusCodeException statusCodeException = ex;
        //config not found
        if (ex.getStatusCode() == 404) {
          notFound = true;
          String message = String.format(
                  "Could not find config for namespace - appId: %s, cluster: %s, namespace: %s, " +
                          "please check whether the configs are released in Zoo!",
                  appId, cluster, m_name);
          statusCodeException = new ZooStatusCodeException(ex.getStatusCode(),
                  message);
        }
        Tracer.logEvent("ZooException", ExceptionUtil.getDetailMessage(statusCodeException));
        transaction.setStatus(statusCodeException);
        exception = statusCodeException;

      } catch (Throwable ex) {
        transaction.setStatus(ex);
        exception = ex;
      } finally {
        transaction.complete();
      }

      if (notFound)
        return null;

      // if force refresh, do normal sleep, if normal config load, do exponential sleep
      onErrorSleepTime = m_datumNeedForceRefresh.get() ? m_configUtil.getOnErrorRetryInterval() :
              m_loadDatumFailSchedulePolicy.fail();
    }
    String message = String.format(
            "Load Zoo Config failed - appId: %s, cluster: %s, namespace: %s, url: %s",
            appId, cluster, m_name, url);
    throw new ZooException(message, exception);
  }

  String assembleQueryDatumUrl(String uri, Notification remoteMessages, NotificationMessage previousDatum)
  {
    String appName = m_options.getClientName();

    if (!uri.endsWith("/"))
    {
      uri += "/";
    }

    String path = "datas/%s";
    List<String> pathParams =
            Lists.newArrayList(
                    pathEscaper.escape(m_name));
    Map<String, String> queryParams = Maps.newHashMap();

    if (!Strings.isNullOrEmpty(appName)) {
      queryParams.put("appName", queryParamEscaper.escape(appName));
    }

    if (previousDatum != null) {
      queryParams.put("md5", queryParamEscaper.escape(previousDatum.getMd5()));
    }

    if (remoteMessages != null)
    {
      queryParams.put("notifyMd5", queryParamEscaper.escape(remoteMessages.getMd5()));
    }

    String localIp = m_configUtil.getLocalIp();
    if (!Strings.isNullOrEmpty(localIp)) {
      queryParams.put("ip", queryParamEscaper.escape(localIp));
    }

    String pathExpanded = String.format(path, pathParams.toArray());

    if (!queryParams.isEmpty()) {
      pathExpanded += "?" + MAP_JOINER.join(queryParams);
    }
    if (!uri.endsWith("/")) {
      uri += "/";
    }
    return uri + pathExpanded;
  }


  public void onNotified(String signalrServer, Notification remoteMessages)
  {
    m_remoteCache.set(remoteMessages);

    m_executorService.submit(new Runnable() {
      @Override
      public void run() {
        try
        {
          trySync();
        }
        catch (Throwable ex)
        {
          logger.warn("Sync config failed, will retry. reason: "+ex.getMessage(), ex);
        }
      }
    });

  }

  public void onNotifiedMessage(NotificationMessage remoteMsg)
  {
    NotificationMessage previous = m_cache.get();
    NotificationMessage current = remoteMsg;

    if (!previous.equals(current))
    {
      logger.debug("Remote data refreshed!");
      m_cache.set(current);
      fireRepositoryChange(m_name, this.getDatum());
    }
  }



  @Override
  public void setUpstreamRepository(DatumRepository upstreamConfigRepository) {
    //remote config doesn't need upstream
  }

//
//  @Override
//  public ConfigSourceType getSourceType() {
//    return ConfigSourceType.REMOTE;
//  }
//
//  private Properties transformZooConfigToProperties(ZooConfig zooConfig) {
//    Properties result = propertiesFactory.getPropertiesInstance();
//    result.putAll(zooConfig.getConfigurations());
//    return result;
//  }
//
//  private ZooConfig loadZooConfig() {
//    if (!m_loadConfigRateLimiter.tryAcquire(5, TimeUnit.SECONDS)) {
//      //wait at most 5 seconds
//      try {
//        TimeUnit.SECONDS.sleep(5);
//      } catch (InterruptedException e) {
//      }
//    }
//    String appId = m_configUtil.getAppId();
//    String cluster = m_configUtil.getCluster();
//    String dataCenter = m_configUtil.getDataCenter();
//    String secret = m_configUtil.getAccessKeySecret();
//    Tracer.logEvent("Zoo.Client.ConfigMeta", STRING_JOINER.join(appId, cluster, m_namespace));
//    int maxRetries = m_configNeedForceRefresh.get() ? 2 : 1;
//    long onErrorSleepTime = 0; // 0 means no sleep
//    Throwable exception = null;
//
//    List<ServiceDTO> configServices = getConfigServices();
//    String url = null;
//    retryLoopLabel:
//    for (int i = 0; i < maxRetries; i++) {
//      List<ServiceDTO> randomConfigServices = Lists.newLinkedList(configServices);
//      Collections.shuffle(randomConfigServices);
//      //Access the server which notifies the client first
//      if (m_longPollServiceDto.get() != null) {
//        randomConfigServices.add(0, m_longPollServiceDto.getAndSet(null));
//      }
//
//      for (ServiceDTO configService : randomConfigServices) {
//        if (onErrorSleepTime > 0) {
//          logger.warn(
//              "Load config failed, will retry in {} {}. appId: {}, cluster: {}, namespaces: {}",
//              onErrorSleepTime, m_configUtil.getOnErrorRetryIntervalTimeUnit(), appId, cluster, m_namespace);
//
//          try {
//            m_configUtil.getOnErrorRetryIntervalTimeUnit().sleep(onErrorSleepTime);
//          } catch (InterruptedException e) {
//            //ignore
//          }
//        }
//
//        url = assembleQueryConfigUrl(configService.getHomepageUrl(), appId, cluster, m_namespace,
//                dataCenter, m_remoteMessages.get(), m_configCache.get());
//
//        logger.debug("Loading config from {}", url);
//
//        HttpRequest request = new HttpRequest(url);
//        if (!StringUtils.isBlank(secret)) {
//          Map<String, String> headers = Signature.buildHttpHeaders(url, appId, secret);
//          request.setHeaders(headers);
//        }
//
//        Transaction transaction = Tracer.newTransaction("Zoo.ConfigService", "queryConfig");
//        transaction.addData("Url", url);
//        try {
//
//          HttpResponse<ZooConfig> response = m_httpClient.doGet(request, ZooConfig.class);
//          m_configNeedForceRefresh.set(false);
//          m_loadConfigFailSchedulePolicy.success();
//
//          transaction.addData("StatusCode", response.getStatusCode());
//          transaction.setStatus(Transaction.SUCCESS);
//
//          if (response.getStatusCode() == 304) {
//            logger.debug("Config server responds with 304 HTTP status code.");
//            return m_configCache.get();
//          }
//
//          ZooConfig result = response.getBody();
//
//          logger.debug("Loaded config for {}: {}", m_namespace, result);
//
//          return result;
//        } catch (ZooStatusCodeException ex) {
//          ZooStatusCodeException statusCodeException = ex;
//          //config not found
//          if (ex.getStatusCode() == 404) {
//            String message = String.format(
//                "Could not find config for namespace - appId: %s, cluster: %s, namespace: %s, " +
//                    "please check whether the configs are released in Zoo!",
//                appId, cluster, m_namespace);
//            statusCodeException = new ZooStatusCodeException(ex.getStatusCode(),
//                message);
//          }
//          Tracer.logEvent("ZooException", ExceptionUtil.getDetailMessage(statusCodeException));
//          transaction.setStatus(statusCodeException);
//          exception = statusCodeException;
//          if(ex.getStatusCode() == 404) {
//            break retryLoopLabel;
//          }
//        } catch (Throwable ex) {
//          Tracer.logEvent("ZooException", ExceptionUtil.getDetailMessage(ex));
//          transaction.setStatus(ex);
//          exception = ex;
//        } finally {
//          transaction.complete();
//        }
//
//        // if force refresh, do normal sleep, if normal config load, do exponential sleep
//        onErrorSleepTime = m_configNeedForceRefresh.get() ? m_configUtil.getOnErrorRetryInterval() :
//            m_loadConfigFailSchedulePolicy.fail();
//      }
//
//    }
//    String message = String.format(
//        "Load Zoo Config failed - appId: %s, cluster: %s, namespace: %s, url: %s",
//        appId, cluster, m_namespace, url);
//    throw new ZooException(message, exception);
//  }
//
//  String assembleQueryConfigUrl(String uri, String appId, String cluster, String namespace,
//                               String dataCenter, ZooNotificationMessages remoteMessages, ZooConfig previousDatum) {
//
//    String path = "datas/%s/%s/%s";
//    List<String> pathParams =
//        Lists.newArrayList(pathEscaper.escape(appId), pathEscaper.escape(cluster),
//            pathEscaper.escape(namespace));
//    Map<String, String> queryParams = Maps.newHashMap();
//
//    if (previousDatum != null) {
//      queryParams.put("releaseKey", queryParamEscaper.escape(previousDatum.getReleaseKey()));
//    }
//
//    if (!Strings.isNullOrEmpty(dataCenter)) {
//      queryParams.put("dataCenter", queryParamEscaper.escape(dataCenter));
//    }
//
//    String localIp = m_configUtil.getLocalIp();
//    if (!Strings.isNullOrEmpty(localIp)) {
//      queryParams.put("ip", queryParamEscaper.escape(localIp));
//    }
//
//    String label = m_configUtil.getZooLabel();
//    if (!Strings.isNullOrEmpty(label)) {
//      queryParams.put("label", queryParamEscaper.escape(label));
//    }
//
//    if (remoteMessages != null) {
//      queryParams.put("messages", queryParamEscaper.escape(GSON.toJson(remoteMessages)));
//    }
//
//    String pathExpanded = String.format(path, pathParams.toArray());
//
//    if (!queryParams.isEmpty()) {
//      pathExpanded += "?" + MAP_JOINER.join(queryParams);
//    }
//    if (!uri.endsWith("/")) {
//      uri += "/";
//    }
//    return uri + pathExpanded;
//  }
//
//  private void scheduleLongPollingRefresh() {
//    remoteConfigLongPollService.submit(m_namespace, this);
//  }
//
//  public void onLongPollNotified(ServiceDTO longPollNotifiedServiceDto, ZooNotificationMessages remoteMessages) {
//    m_longPollServiceDto.set(longPollNotifiedServiceDto);
//    m_remoteMessages.set(remoteMessages);
//    m_executorService.submit(new Runnable() {
//      @Override
//      public void run() {
//        m_configNeedForceRefresh.set(true);
//        trySync();
//      }
//    });
//  }
//
//  private List<ServiceDTO> getConfigServices() {
//    List<ServiceDTO> services = m_configServiceLocator.getConfigServices();
//    if (services.size() == 0) {
//      throw new ZooException("No available config service");
//    }
//
//    return services;
//  }


}
