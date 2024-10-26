package com.wxius.framework.zoo.util.factory;

import com.wxius.framework.zoo.core.ZooClientSystemConsts;
import java.util.Properties;

/**
 * Factory interface to construct Properties instances.
 *
 */
public interface PropertiesFactory {

  /**
   * Configuration to keep properties order as same as line order in .yml/.yaml/.properties file.
   */
  String ZOO_PROPERTY_ORDER_ENABLE = ZooClientSystemConsts.ZOO_PROPERTY_ORDER_ENABLE;

  /**
   * <pre>
   * Default implementation:
   * 1. if {@link ZooClientSystemConsts.ZOO_PROPERTY_ORDER_ENABLE} is true return a new
   * instance of {@link com.wxius.framework.zoo.util.OrderedProperties}.
   * 2. else return a new instance of {@link Properties}
   * </pre>
   *
   * @return
   */
  Properties getPropertiesInstance();
}
