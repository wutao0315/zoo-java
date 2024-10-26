package com.wxius.framework.zoo.infrastructure;

import com.wxius.framework.zoo.Constants;
import com.wxius.framework.zoo.ZooOptions;
import com.wxius.framework.zoo.ZooPublishOption;
import com.wxius.framework.zoo.api.ConnectReset;
import com.wxius.framework.zoo.api.Notification;
import com.wxius.framework.zoo.api.NotificationMessage;
import com.wxius.framework.zoo.api.NotificationServerList;
import com.wxius.framework.zoo.build.ZooInjector;
import com.wxius.framework.zoo.core.ConfigConsts;
import com.wxius.framework.zoo.core.schedule.ExponentialSchedulePolicy;
import com.wxius.framework.zoo.core.schedule.SchedulePolicy;
import com.wxius.framework.zoo.core.utils.ZooThreadFactory;
import com.wxius.framework.zoo.exceptions.ZooException;
import com.wxius.framework.zoo.infrastructure.websocket.ClientWebSocket;
import com.wxius.framework.zoo.infrastructure.websocket.ClientWebSocketHandler;
import com.wxius.framework.zoo.tracer.Tracer;
import com.wxius.framework.zoo.tracer.spi.Transaction;
import com.wxius.framework.zoo.util.ConfigUtil;
import com.wxius.framework.zoo.util.ExceptionUtil;
import com.wxius.framework.zoo.util.http.HttpClient;
//import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
//import com.google.common.escape.Escaper;
//import com.google.common.net.UrlEscapers;
//import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.RateLimiter;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketService {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);
//    private static final Joiner STRING_JOINER = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR);
//    private static final Joiner.MapJoiner MAP_JOINER = Joiner.on("&").withKeyValueSeparator("=");
//    private static final Escaper queryParamEscaper = UrlEscapers.urlFormParameterEscaper();
    private static final long INIT_NOTIFICATION_ID = ConfigConsts.NOTIFICATION_ID_PLACEHOLDER;
    //90 seconds, should be longer than server side's long polling timeout, which is now 60 seconds
    private static final int WEBSOCKET_READ_TIMEOUT = 90 * 1000;
    private final ExecutorService m_websocketService;
//    private final ExecutorService m_websocketReceiveService;
    private final AtomicBoolean m_websocketStopped;
    private SchedulePolicy m_websocketFailSchedulePolicyInSecond;
    private RateLimiter m_websocketRateLimiter;
    private final AtomicBoolean m_websocketStarted;
    private final ConcurrentMap<String, HttpRepository> m_httpRepositorys;
    private final ConcurrentMap<String, Long> m_remoteNotification;
    private final Map<String, Notification> m_remoteNotificationMessages;//namespaceName -> watchedKey -> notificationId
    private Type m_responseType;
    private static final Gson GSON = new Gson();
    private ConfigUtil m_configUtil;
    private HttpClient m_httpClient;
//    private ConfigServiceLocator m_serviceLocator;

    private ZooOptions m_options;
    private ServerListLocator m_serverLocator;
    private volatile String lastServer = null;
    private volatile ClientWebSocket connection = null;

    private volatile boolean isClosed = false;
    private final int sleepTimeConnected = 30000;
    private volatile int sleepTime = sleepTimeConnected;
    private final int sleepTimeReconnecting = 1000;

//    private final ConfigServiceLoadBalancerClient configServiceLoadBalancerClient = ServiceBootstrap.loadPrimary(
//            ConfigServiceLoadBalancerClient.class);

    /**
     * Constructor.
     */
    public WebSocketService(ServerListLocator serviceLocator,
                            ZooOptions options) {
        m_options = options;
        m_serverLocator = serviceLocator;

        m_websocketFailSchedulePolicyInSecond = new ExponentialSchedulePolicy(1, 120); //in second
        m_websocketStopped = new AtomicBoolean(false);
        m_websocketService = Executors.newSingleThreadExecutor(
                ZooThreadFactory.create("ClientWebSocketService", true));
//        m_websocketReceiveService = Executors.newSingleThreadExecutor(
//                ZooThreadFactory.create("ClientWebSocketReciveService", true));
        m_websocketStarted = new AtomicBoolean(false);
        m_httpRepositorys = Maps.newConcurrentMap();
        m_remoteNotification = Maps.newConcurrentMap();
        m_remoteNotificationMessages = Maps.newConcurrentMap();
//        m_responseType = new TypeToken<List<ZooConfigNotification>>() {
//        }.getType();
        m_configUtil = ZooInjector.getInstance(ConfigUtil.class);
        m_httpClient = ZooInjector.getInstance(HttpClient.class);
//        m_serviceLocator = ZooInjector.getInstance(ConfigServiceLocator.class);
        m_websocketRateLimiter = RateLimiter.create(m_configUtil.getLongPollQPS());
    }

    public void submit(String datumName, HttpRepository remoteConfigRepository) {
        m_httpRepositorys.putIfAbsent(datumName, remoteConfigRepository);

        //如果websocket链接正常直接发送
        if (connection != null && connection.isOpened())
        {
            Map<String,Object> data = new HashMap<>();
            data.put("Method","Subscribe");
            data.put("Data", GSON.toJson(new String[] {datumName}));

            String msg = GSON.toJson(data);
            connection.sendMessage(msg);
        }

        m_remoteNotification.putIfAbsent(datumName, INIT_NOTIFICATION_ID);
        if (!m_websocketStarted.get()) {
            startWebSocket();
        }
    }

    private void startWebSocket() {
        if (!m_websocketStarted.compareAndSet(false, true)) {
            //already started
            return;
        }
        try {
            final String appId = m_configUtil.getAppId();
            final String cluster = m_configUtil.getCluster();
            final String dataCenter = m_configUtil.getDataCenter();
            final String secret = m_configUtil.getAccessKeySecret();
            final long websocketInitialDelayInMills = m_configUtil.getLongPollingInitialDelayInMills();
            m_websocketService.submit(new Runnable() {
                @Override
                public void run() {
                    if (websocketInitialDelayInMills > 0) {
                        try {
                            logger.debug("websocket will start in {} ms.", websocketInitialDelayInMills);
                            TimeUnit.MILLISECONDS.sleep(websocketInitialDelayInMills);
                        } catch (InterruptedException e) {
                            //ignore
                        }
                    }
                    doWebSocketCheck();
                }
            });


        } catch (Throwable ex) {
            m_websocketStarted.set(false);
            ZooException exception =
                    new ZooException("Schedule long polling refresh failed", ex);
            Tracer.logError(exception);
            logger.warn(ExceptionUtil.getDetailMessage(exception));
        }
    }

    void stopWebsocketRefresh() {
        this.m_websocketStopped.compareAndSet(false, true);
    }

    private void BatchHeartBeat()
    {
        if (connection == null || !connection.isOpened())
        {
            return;
        }

        Map<String,String> datums = new HashMap<>();

        for (Map.Entry<String, HttpRepository> entry: m_httpRepositorys.entrySet())
        {
            String md5 = entry.getValue().getCache() == null?"":entry.getValue().getCache().getMd5();
            datums.put(entry.getKey(), md5);
        }

        List<String> instances = new ArrayList<>();
        for (ZooPublishOption item: m_options.getPublish())
        {
            instances.add(item.toString());
        }

        Map<String,Object> data = new HashMap<>();
        data.put("Method","HeartBeat");
        Map<String,Object> dataInner = new HashMap<>();
        dataInner.put("NamespaceId",m_options.getNamespaceId());
        dataInner.put("GroupName",m_options.getGroupName());
        dataInner.put("Md5",m_serverLocator.getMd5());
        dataInner.put("Datums",datums);
        dataInner.put("Instances",instances);
        data.put("Data", GSON.toJson(dataInner));

        String msg = GSON.toJson(data);
        connection.sendMessage(msg);
    }
    private void doWebSocketCheck()
    {
        while (!m_websocketStopped.get() && !Thread.currentThread().isInterrupted()) {
            if (!m_websocketRateLimiter.tryAcquire(5, TimeUnit.SECONDS)) {
                //wait at most 5 seconds
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                }
            }
            String url = "";
            Transaction transaction = Tracer.newTransaction("Zoo.doWebSocketCheck", "websocketNotification");
            try {
                if (connection != null
                        && connection.isOpened())
                {
                    logger.info("--------------State:"+sleepTime);
                    BatchHeartBeat();
                    continue;
                }

                if (connection != null && isClosed)
                {
                    throw new ZooException("connection is closed");
                }


                if (lastServer == null || lastServer.isEmpty()) {
                    lastServer = m_serverLocator.getNextServer();
                }

                if (lastServer == null || lastServer.isEmpty())
                {
                    continue;
                }

                url = WebSocketUrl(lastServer);
                logger.debug("websocket from {}", url);

                connection = new ClientWebSocket(m_options);

                connection.connect(url);

                connection.addMessageHandler(new ClientWebSocketHandler(this));

                isClosed = false;
                sleepTime = sleepTimeConnected;
                m_websocketFailSchedulePolicyInSecond.success();
                transaction.setStatus(Transaction.SUCCESS);
                logger.info("--------------connect");
                //注册服务实例
                if (m_options.getPublish().size() > 0)
                {
                    List<String> instances = new ArrayList<>();
                    for (ZooPublishOption item: m_options.getPublish())
                    {
                        instances.add(item.toString());
                    }
                    String dataStr = GSON.toJson(instances);
                    Map<String,Object> data = new HashMap<>();
                    data.put("Method","Register");
                    data.put("Data",dataStr);
                    String msg = GSON.toJson(data);
                    connection.sendMessage(msg);
                }
                //订阅配置和服务

                Map<String,Object> data = new HashMap<>();
                data.put("Method","Subscribe");
                data.put("Data", GSON.toJson(m_httpRepositorys.keySet()));
                String msg = GSON.toJson(data);
                connection.sendMessage(msg);
                transaction.addData("Url", url);

            } catch (Throwable ex) {
                if(connection!=null)
                {
                    connection.disconnect();
                }
                connection = null;
                lastServer = null;
                Tracer.logEvent("ZooException", ExceptionUtil.getDetailMessage(ex));
                transaction.setStatus(ex);
                long sleepTimeInSecond = m_websocketFailSchedulePolicyInSecond.fail();
                logger.warn(
                        "Websocket failed, will retry in {} seconds. appId: {}, cluster: {}, namespaces: {}, websocket url: {}, reason: {}",
                        sleepTimeInSecond, m_options.getAccessKey(), m_options.getGroupName(), m_options.getNamespaceId(), url, ExceptionUtil.getDetailMessage(ex));
                try {
                    TimeUnit.SECONDS.sleep(sleepTimeInSecond);
                } catch (InterruptedException ie) {
                    //ignore
                }
            } finally {
                transaction.complete();
            }
        }

    }


    //接收更新后的服务器地址
    public void notificationServerList(NotificationServerList notification)
    {
        try
        {
            notifyServerList(notification.getNodes(), notification.getMd5());
            logger.debug("notificationServerList:"+GSON.toJson(notification));
        }
        catch (Throwable ex)
        {
            logger.warn("Sync server list failed, will retry. reason: "+ex.getMessage(), ex);
        }
    }
    //接收更新后的配置提醒
    public void notification(List<Notification> notifications)
    {
        try
        {
            notify(lastServer, notifications);
            logger.debug("notification:"+ GSON.toJson(notifications));
        }
        catch (Throwable ex)
        {
            logger.warn("Sync datum failed, will retry. reason: "+ex.getMessage(), ex);
        }
    };
    //接收更新后的配置明细
    public void notificationMessage(List<NotificationMessage> notificationMessages)
    {
        try
        {
            notifyMessage(lastServer, notificationMessages);
            logger.debug("notificationMessage:"+GSON.toJson(notificationMessages));
        }
        catch (Throwable ex)
        {
            logger.warn("Sync notification Message failed, will retry.  reason:"+ex.getMessage(),ex);
        }
    };
    //服务器主动出发下线
    public void offline()
    {
        try
        {
            if(connection!=null)
            {
                connection.disconnect();
            }
            stopWebsocketRefresh();
            isClosed = true;
            logger.debug("server offline call");
        }
        catch (Throwable ex)
        {
            logger.warn("offline failed, reason: "+ex.getMessage(),ex);
        }
    };
    //切换配置中心
    public void reset(ConnectReset reset)
    {
        try
        {
            if(connection!=null)
            {
                connection.disconnect();
            }
            connection = null;

            if (reset!=null
                    && reset.getServerIp() != null
                    && !reset.getServerIp().isEmpty())
            {
                lastServer = String.format("{}://{}{}{}",reset.getScheme(),reset.getServerIp(), Constants.COLON,reset.getServerPort());
            }
            else
            {
                lastServer = null;
            }
            logger.debug("server reset call");
        }
        catch (Throwable ex)
        {
            logger.warn("reset failed, reason:"+ex.getMessage(), ex);
        }
    };
    //服务器探活
    public void detection()
    {
        try
        {
            BatchHeartBeat();
            logger.debug("server call batch heatbeat");
        }
        catch (Throwable ex)
        {
            logger.warn("server call batch heatbeat failed, reason: "+ex.getMessage(), ex);
        }
    };

    private void notify(String lastServer, List<Notification> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return;
        }
        for (Notification notification : notifications) {
            String name = notification.getName();

            //不包含拉取对象忽略
            if (!m_httpRepositorys.containsKey(notification.getName()) || m_httpRepositorys.get(notification.getName()) == null)
            {
                continue;
            }
            HttpRepository repository = m_httpRepositorys.get(notification.getName());
            //避免网络延迟导致的历史通知
            if (!m_remoteNotification.containsKey(notification.getName())
                    || m_remoteNotification.get(notification.getName()) >= notification.getId())
            {
                continue;
            }

            //避免不必要的拉取
            if (repository.getCache()!=null && repository.getCache().getMd5() == notification.getMd5())
            {
                continue;
            }

            try {
                repository.onNotified(lastServer, notification);
                //通知完成,更新提醒id
                m_remoteNotification.put(notification.getName(), notification.getId());
            } catch (Throwable ex) {
                Tracer.logError(ex);
            }

        }
    }

    //远程推变更内容
    private void notifyMessage(String lastServer, List<NotificationMessage> notificationMessages)
    {
        if (notificationMessages == null || notificationMessages.size() == 0) return;

        for (NotificationMessage notificationMessage : notificationMessages)
        {
            //不包含拉取对象忽略
            if (!m_httpRepositorys.containsKey(notificationMessage.getName())  || m_httpRepositorys.get(notificationMessage.getName()) == null)
            {
                return;
            }
            HttpRepository repository = m_httpRepositorys.get(notificationMessage.getName());
            //避免网络延迟导致的历史通知
            if (!m_remoteNotification.containsKey(notificationMessage.getName())
                    || m_remoteNotification.get(notificationMessage.getName()) >= notificationMessage.getId())
            {
                return;
            }
            //避免不必要的拉取
            if (repository.getCache() != null
                    && repository.getCache().getMd5() == notificationMessage.getMd5())
            {
                return;
            }

            try
            {
                repository.onNotifiedMessage(notificationMessage);
                //通知完成,更新提醒id
                m_remoteNotification.put(notificationMessage.getName(), notificationMessage.getId());
            }
            catch (Throwable ex)
            {
                logger.warn(ex.getMessage(), ex);
            }
        }
    }
    // 远程推变更的注册发现中心地址
    private void notifyServerList(List<String> serverList, String md5)
    {
        if (serverList == null
                || serverList.size() == 0
                || md5 == null
                || md5.isEmpty()
                || md5.toLowerCase().equals(m_serverLocator.getMd5().toLowerCase()))
        {
            return;
        }

        m_serverLocator.OnNotified(serverList, md5);
    }


    //路径拼接
    String WebSocketUrl(String lastServer)
    {
        lastServer = lastServer.toLowerCase();
        String url = "";
        if (lastServer.startsWith("https:"))
        {
            url = "wss:" + lastServer.substring(6,lastServer.length());
        }
        else if (lastServer.startsWith("http:"))
        {
            url = "ws:" + lastServer.substring(5,lastServer.length());
        }
        else
        {
            url = "ws://" + lastServer;
        }

        if (!url.endsWith("/")) url += "/";

        return url + "ws";
    }

}


