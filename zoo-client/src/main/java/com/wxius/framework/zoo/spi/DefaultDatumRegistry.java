package com.wxius.framework.zoo.spi;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class DefaultDatumRegistry implements DatumRegistry {
  private static final Logger s_logger = LoggerFactory.getLogger(DefaultDatumRegistry.class);
  private Map<String, DatumFactory> m_instances = Maps.newConcurrentMap();

  @Override
  public void register(String namespace, DatumFactory factory) {
    if (m_instances.containsKey(namespace)) {
      s_logger.warn("ConfigFactory({}) is overridden by {}!", namespace, factory.getClass());
    }

    m_instances.put(namespace, factory);
  }

  @Override
  public DatumFactory getFactory(String namespace) {
    DatumFactory config = m_instances.get(namespace);

    return config;
  }
}
