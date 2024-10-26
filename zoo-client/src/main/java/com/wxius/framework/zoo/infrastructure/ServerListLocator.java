package com.wxius.framework.zoo.infrastructure;

import com.wxius.framework.zoo.ZooOptions;
import com.wxius.framework.zoo.exceptions.ZooException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class ServerListLocator {
    private static final Logger logger = LoggerFactory.getLogger(ServerListLocator.class);
    private ZooOptions m_options;
    private List<String> m_initServerList;
    private AtomicReference<List<String>> m_serverList;

    private volatile String m_md5 = "";
    private volatile String m_currentServer = "";


    public ServerListLocator(ZooOptions options)
    {
        m_options = options;
        m_initServerList = GetCustomizedServerList(options);
        m_serverList = new AtomicReference<>(m_initServerList);
    }

    List<String> GetCustomizedServerList(ZooOptions options)
    {
        if (options.getServerAddresses().size()<=0)
        {
            throw new ZooException("ServerAddresses is null, must have value");
        }

        List<String> result = new ArrayList<>();
        for (String item : options.getServerAddresses())
        {
            result.add(item.endsWith("/")?item.substring(0,item.length()):item);
        }

        return result;
    }

    public String getCurrentServer()
    {
        if (m_currentServer == null || m_currentServer.isEmpty())
        {
            RefreshCurrentServer();
        }
        return m_currentServer;
    }
    public void setCurrentServer(String currentServer)
    {
        m_currentServer = currentServer;
    }

    public String getNextServer()
    {
        RefreshCurrentServer();
        return m_currentServer;
    }
    public String getMd5()
    {
        return m_md5;
    }
    public void RefreshCurrentServer()
    {
        Random random = new Random();
        int index = random.nextInt(m_serverList.get().size());
        m_currentServer = m_serverList.get().get(index);

        if (m_currentServer == null || m_currentServer.isEmpty())
        {
            index = random.nextInt(m_initServerList.size());
            m_currentServer = m_initServerList.get(index);
        }
    }
    public void OnNotified(List<String> remoteServers, String md5)
    {
        m_serverList.get().clear();
        for (String item : remoteServers)
        {
            String data = item.endsWith("/")?item.substring(0,item.length()):item;
            if (!item.contains("://"))
            {
                data += m_options.getScheme()+"://";
            }
            m_serverList.get().add(data);
        }
        m_md5 = md5;
    }

}
