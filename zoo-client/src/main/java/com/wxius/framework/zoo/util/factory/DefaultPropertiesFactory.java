
package com.wxius.framework.zoo.util.factory;

import com.wxius.framework.zoo.build.ZooInjector;
import com.wxius.framework.zoo.util.ConfigUtil;
import com.wxius.framework.zoo.util.OrderedProperties;
import java.util.Properties;

/**
 * Default PropertiesFactory implementation.
 *
 */
public class DefaultPropertiesFactory implements PropertiesFactory {

  private ConfigUtil m_configUtil;

  public DefaultPropertiesFactory() {
    m_configUtil = ZooInjector.getInstance(ConfigUtil.class);
  }

  @Override
  public Properties getPropertiesInstance() {
    if (m_configUtil.isPropertiesOrderEnabled()) {
      return new OrderedProperties();
    } else {
      return new Properties();
    }
  }
}
