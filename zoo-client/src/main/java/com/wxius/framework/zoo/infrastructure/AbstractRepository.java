package com.wxius.framework.zoo.infrastructure;

import com.wxius.framework.zoo.build.ZooInjector;
import com.wxius.framework.zoo.util.factory.PropertiesFactory;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wxius.framework.zoo.tracer.Tracer;
import com.wxius.framework.zoo.util.ExceptionUtil;
import com.google.common.collect.Lists;

public abstract class AbstractRepository implements DatumRepository {
  private static final Logger logger = LoggerFactory.getLogger(AbstractRepository.class);
  private List<RepositoryChangeListener> m_listeners = Lists.newCopyOnWriteArrayList();
  protected PropertiesFactory propertiesFactory = ZooInjector.getInstance(PropertiesFactory.class);

  protected boolean trySync() {
    try {
      sync();
      return true;
    } catch (Throwable ex) {
      Tracer.logEvent("ZooException", ExceptionUtil.getDetailMessage(ex));
      logger
          .warn("Sync config failed, will retry. Repository {}, reason: {}", this.getClass(), ExceptionUtil
              .getDetailMessage(ex));
    }
    return false;
  }

  protected abstract void sync();

  @Override
  public void addChangeListener(RepositoryChangeListener listener) {
    if (!m_listeners.contains(listener)) {
      m_listeners.add(listener);
    }
  }

  @Override
  public void removeChangeListener(RepositoryChangeListener listener) {
    m_listeners.remove(listener);
  }

  protected void fireRepositoryChange(String namespace, Properties newProperties) {
    for (RepositoryChangeListener listener : m_listeners) {
      try {
        listener.onRepositoryChange(namespace, newProperties);
      } catch (Throwable ex) {
        Tracer.logError(ex);
        logger.error("Failed to invoke repository change listener {}", listener.getClass(), ex);
      }
    }
  }
}
