package com.wxius.framework.zoo.infrastructure;

import java.util.Map;

import com.wxius.framework.zoo.Datum;
//import com.wxius.framework.zoo.ConfigFile;
import com.wxius.framework.zoo.build.ZooInjector;
//import com.wxius.framework.zoo.core.enums.ConfigFileFormat;
import com.wxius.framework.zoo.spi.DatumFactory;
import com.wxius.framework.zoo.spi.DatumFactoryManager;
import com.google.common.collect.Maps;

public class DefaultDatumManager implements DatumManager {
  private DatumFactoryManager m_factoryManager;
  private Map<String, Datum> m_configs = Maps.newConcurrentMap();
  private Map<String, Object> m_configLocks = Maps.newConcurrentMap();

  public DefaultDatumManager() {
    m_factoryManager = ZooInjector.getInstance(DatumFactoryManager.class);
  }

  @Override
  public Datum getDatum(String name) {
    Datum config = m_configs.get(name);

    if (config == null) {
      Object lock = m_configLocks.computeIfAbsent(name, key -> new Object());
      synchronized (lock) {
        config = m_configs.get(name);

        if (config == null) {
          DatumFactory factory = m_factoryManager.getFactory(name);

          config = factory.create(name);
          m_configs.put(name, config);
        }
      }
    }

    return config;
  }
}
