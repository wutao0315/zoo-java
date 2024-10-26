package com.wxius.framework.zoo.infrastructure;

import com.wxius.framework.zoo.exceptions.ZooException;
import com.wxius.framework.zoo.spi.ZooInjectorCustomizer;
import com.wxius.framework.zoo.spi.DatumFactory;
import com.wxius.framework.zoo.spi.DatumFactoryManager;
import com.wxius.framework.zoo.spi.DatumRegistry;
import com.wxius.framework.zoo.spi.DefaultDatumFactory;
import com.wxius.framework.zoo.spi.DefaultDatumFactoryManager;
import com.wxius.framework.zoo.spi.DefaultDatumRegistry;
import com.wxius.framework.zoo.tracer.Tracer;
import com.wxius.framework.zoo.util.AESCipher;
import com.wxius.framework.zoo.util.ConfigUtil;
import com.wxius.framework.zoo.util.factory.DefaultPropertiesFactory;
import com.wxius.framework.zoo.util.factory.PropertiesFactory;
import com.wxius.framework.zoo.util.http.DefaultHttpClient;
import com.wxius.framework.zoo.util.http.HttpClient;

//import com.wxius.framework.zoo.util.yaml.YamlParser;
import com.wxius.framework.foundation.internals.ServiceBootstrap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Singleton;
import java.util.List;

/**
 * Guice injector
 */
public class DefaultInjector implements Injector {
  private final com.google.inject.Injector m_injector;
  private final List<ZooInjectorCustomizer> m_customizers;

  public DefaultInjector() {
    try {
      m_injector = Guice.createInjector(new ZooModule());
      m_customizers = ServiceBootstrap.loadAllOrdered(ZooInjectorCustomizer.class);
    } catch (Throwable ex) {
      ZooException exception = new ZooException("Unable to initialize Guice Injector!", ex);
      Tracer.logError(exception);
      throw exception;
    }
  }

  @Override
  public <T> T getInstance(Class<T> clazz) {
    try {
      for (ZooInjectorCustomizer customizer : m_customizers) {
        T instance = customizer.getInstance(clazz);
        if (instance != null) {
          return instance;
        }
      }
      return m_injector.getInstance(clazz);
    } catch (Throwable ex) {
      Tracer.logError(ex);
      throw new ZooException(
          String.format("Unable to load instance for %s!", clazz.getName()), ex);
    }
  }

  @Override
  public <T> T getInstance(Class<T> clazz, String name) {
    try {
      for (ZooInjectorCustomizer customizer : m_customizers) {
        T instance = customizer.getInstance(clazz, name);
        if (instance != null) {
          return instance;
        }
      }
      //Guice does not support get instance by type and name
      return null;
    } catch (Throwable ex) {
      Tracer.logError(ex);
      throw new ZooException(
          String.format("Unable to load instance for %s with name %s!", clazz.getName(), name), ex);
    }
  }

  private static class ZooModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(DatumManager.class).to(DefaultDatumManager.class).in(Singleton.class);
      bind(DatumFactoryManager.class).to(DefaultDatumFactoryManager.class).in(Singleton.class);
      bind(DatumRegistry.class).to(DefaultDatumRegistry.class).in(Singleton.class);
      bind(DatumFactory.class).to(DefaultDatumFactory.class).in(Singleton.class);
      bind(ConfigUtil.class).in(Singleton.class);
      bind(HttpClient.class).to(DefaultHttpClient.class).in(Singleton.class);
//      bind(ConfigServiceLocator.class).in(Singleton.class);
//      bind(RemoteConfigLongPollService.class).in(Singleton.class);
//      bind(YamlParser.class).in(Singleton.class);
      bind(PropertiesFactory.class).to(DefaultPropertiesFactory.class).in(Singleton.class);
      bind(AESCipher.class).to(AESCipher.class).in(Singleton.class);
    }
  }
}
