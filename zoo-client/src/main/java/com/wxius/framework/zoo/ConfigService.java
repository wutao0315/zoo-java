package com.wxius.framework.zoo;

import com.wxius.framework.zoo.build.ZooInjector;
import com.wxius.framework.zoo.core.ConfigConsts;
//import com.wxius.framework.zoo.core.enums.ConfigFileFormat;
import com.wxius.framework.zoo.infrastructure.DatumManager;
import com.wxius.framework.zoo.spi.DatumFactory;
import com.wxius.framework.zoo.spi.DatumRegistry;

/**
 * Entry point for client config use
 *
 */
public class ConfigService {
  private static final ConfigService s_instance = new ConfigService();

  private volatile DatumManager m_configManager;
  private volatile DatumRegistry m_configRegistry;

  private DatumManager getManager() {
    if (m_configManager == null) {
      synchronized (this) {
        if (m_configManager == null) {
          m_configManager = ZooInjector.getInstance(DatumManager.class);
        }
      }
    }

    return m_configManager;
  }

  private DatumRegistry getRegistry() {
    if (m_configRegistry == null) {
      synchronized (this) {
        if (m_configRegistry == null) {
          m_configRegistry = ZooInjector.getInstance(DatumRegistry.class);
        }
      }
    }

    return m_configRegistry;
  }

  /**
   * Get Application's config instance.
   *
   * @return config instance
   */
  public static Datum getAppConfig() {
    return getConfig(ConfigConsts.NAMESPACE_APPLICATION);
  }

  /**
   * Get the config instance for the namespace.
   *
   * @param name the namespace of the config
   * @return config instance
   */
  public static Datum getConfig(String name) {
    return s_instance.getManager().getDatum(name);
  }

  static void setConfig(Datum config) {
    setConfig(ConfigConsts.NAMESPACE_APPLICATION, config);
  }

  /**
   * Manually set the config for the namespace specified, use with caution.
   *
   * @param name the namespace
   * @param config    the config instance
   */
  static void setConfig(String name, final Datum config) {
    s_instance.getRegistry().register(name, new DatumFactory() {
      @Override
      public Datum create(String name) {
        return config;
      }

    });
  }

  static void setConfigFactory(DatumFactory factory) {
    setConfigFactory(ConfigConsts.NAMESPACE_APPLICATION, factory);
  }

  /**
   * Manually set the config factory for the namespace specified, use with caution.
   *
   * @param name the namespace
   * @param factory   the factory instance
   */
  static void setConfigFactory(String name, DatumFactory factory) {
    s_instance.getRegistry().register(name, factory);
  }

  // for test only
  static void reset() {
    synchronized (s_instance) {
      s_instance.m_configManager = null;
      s_instance.m_configRegistry = null;
    }
  }
}
