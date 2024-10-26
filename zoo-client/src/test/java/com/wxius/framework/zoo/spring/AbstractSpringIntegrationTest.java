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
package com.wxius.framework.zoo.spring;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.wxius.framework.zoo.core.ConfigConsts;
import com.wxius.framework.zoo.infrastructure.DatumRepository;
import com.wxius.framework.zoo.infrastructure.DefaultInjector;
//import com.wxius.framework.zoo.infrastructure.SimpleConfig;
//import com.wxius.framework.zoo.infrastructure.YamlConfigFile;
import com.wxius.framework.zoo.spring.config.PropertySourcesProcessor;
import com.wxius.framework.zoo.util.ConfigUtil;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import java.util.Objects;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.springframework.util.ReflectionUtils;

import com.wxius.framework.zoo.Datum;
//import com.wxius.framework.zoo.ConfigFile;
import com.wxius.framework.zoo.ConfigService;
import com.wxius.framework.zoo.build.MockInjector;
//import com.wxius.framework.zoo.core.enums.ConfigFileFormat;
import com.wxius.framework.zoo.infrastructure.DatumManager;
import com.google.common.collect.Maps;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public abstract class AbstractSpringIntegrationTest {
  private static final Map<String, Datum> CONFIG_REGISTRY = Maps.newHashMap();
//  private static final Map<String, ConfigFile> CONFIG_FILE_REGISTRY = Maps.newHashMap();
  private static Method CONFIG_SERVICE_RESET;
  private static Method PROPERTY_SOURCES_PROCESSOR_RESET;

  static {
    try {
      CONFIG_SERVICE_RESET = ConfigService.class.getDeclaredMethod("reset");
      ReflectionUtils.makeAccessible(CONFIG_SERVICE_RESET);
      PROPERTY_SOURCES_PROCESSOR_RESET = PropertySourcesProcessor.class.getDeclaredMethod("reset");
      ReflectionUtils.makeAccessible(PROPERTY_SOURCES_PROCESSOR_RESET);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
  }

  @Before
  public void setUp() throws Exception {
    doSetUp();
  }

  @After
  public void tearDown() throws Exception {
    doTearDown();
  }

//  protected SimpleConfig prepareConfig(String namespaceName, Properties properties) {
//    DatumRepository configRepository = mock(DatumRepository.class);
//
//    when(configRepository.getDatum()).thenReturn(properties);
//
//    SimpleConfig config = new SimpleConfig(ConfigConsts.NAMESPACE_APPLICATION, configRepository);
//
//    mockConfig(namespaceName, config);
//
//    return config;
//  }

  protected static Properties readYamlContentAsConfigFileProperties(String caseName)
      throws IOException {
    final String filePath = "spring/yaml/" + caseName;
    ClassLoader classLoader = AbstractSpringIntegrationTest.class.getClassLoader();

    InputStream inputStream = classLoader.getResourceAsStream(filePath);
    Objects.requireNonNull(inputStream, filePath + " may be not exist under src/test/resources/");
    String yamlContent = CharStreams
        .toString(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

    Properties properties = new Properties();
    properties.setProperty(ConfigConsts.CONFIG_FILE_CONTENT_KEY, yamlContent);

    return properties;
  }

//  protected static YamlConfigFile prepareYamlConfigFile(String namespaceNameWithFormat, Properties properties) {
//    DatumRepository configRepository = mock(DatumRepository.class);
//
//    when(configRepository.getDatum()).thenReturn(properties);
//
//    // spy it for testing after
//    YamlConfigFile configFile = spy(new YamlConfigFile(namespaceNameWithFormat, configRepository));
//
//    mockConfigFile(namespaceNameWithFormat, configFile);
//
//    return configFile;
//  }

  protected Properties assembleProperties(String key, String value) {
    Properties properties = new Properties();
    properties.setProperty(key, value);

    return properties;
  }

  protected Properties assembleProperties(String key, String value, String key2, String value2) {
    Properties properties = new Properties();
    properties.setProperty(key, value);
    properties.setProperty(key2, value2);

    return properties;
  }

  protected Properties assembleProperties(String key, String value, String key2, String value2,
      String key3, String value3) {

    Properties properties = new Properties();
    properties.setProperty(key, value);
    properties.setProperty(key2, value2);
    properties.setProperty(key3, value3);

    return properties;
  }

  protected Date assembleDate(int year, int month, int day, int hour, int minute, int second, int millisecond) {
    Calendar date = Calendar.getInstance();
    date.set(year, month - 1, day, hour, minute, second); //Month in Calendar is 0 based
    date.set(Calendar.MILLISECOND, millisecond);

    return date.getTime();
  }


  protected static void mockConfig(String namespace, Datum config) {
    CONFIG_REGISTRY.put(namespace, config);
  }

//  protected static void mockConfigFile(String namespaceNameWithFormat, ConfigFile configFile) {
//    CONFIG_FILE_REGISTRY.put(namespaceNameWithFormat, configFile);
//  }

  protected static void doSetUp() {
    //as ConfigService is singleton, so we must manually clear its container
    ReflectionUtils.invokeMethod(CONFIG_SERVICE_RESET, null);
    //as PropertySourcesProcessor has some static variables, so we must manually clear them
    ReflectionUtils.invokeMethod(PROPERTY_SOURCES_PROCESSOR_RESET, null);
    DefaultInjector defaultInjector = new DefaultInjector();
    DatumManager defaultConfigManager = defaultInjector.getInstance(DatumManager.class);
    MockInjector.setInstance(DatumManager.class, new MockConfigManager(defaultConfigManager));
    MockInjector.setDelegate(defaultInjector);
  }

  protected static void doTearDown() {
    MockInjector.reset();
    CONFIG_REGISTRY.clear();
  }

  private static class MockConfigManager implements DatumManager {

    private final DatumManager delegate;

    public MockConfigManager(DatumManager delegate) {
      this.delegate = delegate;
    }

    @Override
    public Datum getDatum(String namespace) {
      Datum config = CONFIG_REGISTRY.get(namespace);
      if (config != null) {
        return config;
      }
      return delegate.getDatum(namespace);
    }

//    @Override
//    public ConfigFile getConfigFile(String namespace, ConfigFileFormat configFileFormat) {
//      ConfigFile configFile = CONFIG_FILE_REGISTRY.get(String.format("%s.%s", namespace, configFileFormat.getValue()));
//      if (configFile != null) {
//        return configFile;
//      }
//      return delegate.getConfigFile(namespace, configFileFormat);
//    }
  }

  protected static class MockConfigUtil extends ConfigUtil {

    private boolean isAutoUpdateInjectedSpringProperties;

    public void setAutoUpdateInjectedSpringProperties(boolean autoUpdateInjectedSpringProperties) {
      isAutoUpdateInjectedSpringProperties = autoUpdateInjectedSpringProperties;
    }

    @Override
    public boolean isAutoUpdateInjectedSpringPropertiesEnabled() {
      return isAutoUpdateInjectedSpringProperties;
    }
  }
}
