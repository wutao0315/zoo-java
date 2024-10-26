package com.wxius.framework.zoo.build;

import com.wxius.framework.zoo.exceptions.ZooException;
import com.wxius.framework.zoo.infrastructure.Injector;
import com.wxius.framework.zoo.tracer.Tracer;
import com.wxius.framework.foundation.internals.ServiceBootstrap;


public class ZooInjector {
  private static volatile Injector s_injector;
  private static final Object lock = new Object();

  private static Injector getInjector() {
    if (s_injector == null) {
      synchronized (lock) {
        if (s_injector == null) {
          try {
            s_injector = ServiceBootstrap.loadPrimary(Injector.class);
          } catch (Throwable ex) {
            ZooException exception = new ZooException("Unable to initialize Zoo Injector!", ex);
            Tracer.logError(exception);
            throw exception;
          }
        }
      }
    }

    return s_injector;
  }

  public static <T> T getInstance(Class<T> clazz) {
    try {
      return getInjector().getInstance(clazz);
    } catch (Throwable ex) {
      Tracer.logError(ex);
      throw new ZooException(String.format("Unable to load instance for type %s!", clazz.getName()), ex);
    }
  }

  public static <T> T getInstance(Class<T> clazz, String name) {
    try {
      return getInjector().getInstance(clazz, name);
    } catch (Throwable ex) {
      Tracer.logError(ex);
      throw new ZooException(
          String.format("Unable to load instance for type %s and name %s !", clazz.getName(), name), ex);
    }
  }
}
