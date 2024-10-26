package com.wxius.framework.zoo.infrastructure;

import com.wxius.framework.zoo.core.spi.Ordered;

public interface Injector extends Ordered {

  /**
   * Returns the appropriate instance for the given injection type
   */
  <T> T getInstance(Class<T> clazz);

  /**
   * Returns the appropriate instance for the given injection type and name
   */
  <T> T getInstance(Class<T> clazz, String name);

  @Override
  default int getOrder() {
    return 0;
  }
}
