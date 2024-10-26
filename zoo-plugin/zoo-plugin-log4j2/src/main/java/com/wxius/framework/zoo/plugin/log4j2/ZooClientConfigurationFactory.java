/*
 * Copyright 2022 Zoo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.wxius.framework.zoo.plugin.log4j2;

//import com.wxius.framework.zoo.ConfigFile;
import com.wxius.framework.zoo.ConfigService;
//import com.wxius.framework.zoo.core.enums.ConfigFileFormat;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationException;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;

/**
 * @author nisiyong
 */
@Plugin(name = "ZooClientConfigurationFactory", category = ConfigurationFactory.CATEGORY)
@Order(50)
public class ZooClientConfigurationFactory extends ConfigurationFactory {

  private final boolean isActive;

  public ZooClientConfigurationFactory() {
    String enabled = System.getProperty("zoo.log4j2.enabled");
    if (enabled == null) {
      enabled = System.getenv("ZOO_LOG4J2_ENABLED");
    }
    isActive = Boolean.parseBoolean(enabled);
  }

  @Override
  protected boolean isActive() {
    return this.isActive;
  }

  @Override
  protected String[] getSupportedTypes() {
    return new String[]{"*"};
  }

  @Override
  public Configuration getConfiguration(LoggerContext loggerContext, String name, URI configLocation) {
    return getConfiguration(loggerContext, null);
  }

  @Override
  public Configuration getConfiguration(LoggerContext loggerContext, ConfigurationSource configurationSource) {
    if (!isActive) {
      LOGGER.warn("Zoo log4j2 plugin is not enabled, please check your configuration");
      return null;
    }

//    ConfigFile configFile = ConfigService.getConfigFile("log4j2", ConfigFileFormat.XML);
//
//    if (configFile == null || configFile.getContent() == null || configFile.getContent().isEmpty()) {
//      LOGGER.warn("Zoo log4j2 plugin is enabled, but no log4j2.xml namespace or content found in Zoo");
//      return null;
//    }
//
//    byte[] bytes = configFile.getContent().getBytes(StandardCharsets.UTF_8);
//    try {
//      configurationSource = new ConfigurationSource(new ByteArrayInputStream(bytes));
//    } catch (IOException e) {
//      throw new ConfigurationException("Unable to initialize ConfigurationSource from Zoo", e);
//    }

    // TODO add ConfigFileChangeListener, dynamic load log4j2.xml in runtime
//    LOGGER.info("Zoo log4j2 plugin is enabled, loading log4j2.xml from Zoo, content:\n{}", configFile.getContent());
    return new XmlConfiguration(loggerContext, configurationSource);
  }
}
