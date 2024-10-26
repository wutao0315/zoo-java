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
package com.wxius.framework.zoo.spring.boot;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.wxius.framework.zoo.build.MockInjector;
import com.wxius.framework.zoo.core.ZooClientSystemConsts;
import com.wxius.framework.zoo.core.ConfigConsts;
import com.wxius.framework.zoo.spring.config.CachedCompositePropertySource;
import com.wxius.framework.zoo.spring.config.PropertySourcesConstants;
import com.wxius.framework.zoo.util.ConfigUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Properties;

public class ZooApplicationContextInitializerTest {

  private ZooApplicationContextInitializer zooApplicationContextInitializer;

  @Before
  public void setUp() throws Exception {
    zooApplicationContextInitializer = new ZooApplicationContextInitializer();
  }

  @After
  public void tearDown() throws Exception {
    System.clearProperty(ZooClientSystemConsts.APP_ID);
    System.clearProperty(ConfigConsts.ZOO_CLUSTER_KEY);
    System.clearProperty(ZooClientSystemConsts.ZOO_CACHE_DIR);
    System.clearProperty(ConfigConsts.ZOO_META_KEY);

    MockInjector.reset();
  }

  @Test
  public void testFillFromEnvironment() throws Exception {
    String someAppId = "someAppId";
    String someCluster = "someCluster";
    String someCacheDir = "someCacheDir";
    String someZooMeta = "someZooMeta";

    ConfigurableEnvironment environment = mock(ConfigurableEnvironment.class);

    when(environment.getProperty(ZooClientSystemConsts.APP_ID)).thenReturn(someAppId);
    when(environment.getProperty(ConfigConsts.ZOO_CLUSTER_KEY)).thenReturn(someCluster);
    when(environment.getProperty(ZooClientSystemConsts.ZOO_CACHE_DIR)).thenReturn(someCacheDir);
    when(environment.getProperty(ConfigConsts.ZOO_META_KEY)).thenReturn(someZooMeta);

    zooApplicationContextInitializer.initializeSystemProperty(environment);

    assertEquals(someAppId, System.getProperty(ZooClientSystemConsts.APP_ID));
    assertEquals(someCluster, System.getProperty(ConfigConsts.ZOO_CLUSTER_KEY));
    assertEquals(someCacheDir, System.getProperty(ZooClientSystemConsts.ZOO_CACHE_DIR));
    assertEquals(someZooMeta, System.getProperty(ConfigConsts.ZOO_META_KEY));
  }

  @Test
  public void testFillFromEnvironmentWithSystemPropertyAlreadyFilled() throws Exception {
    String someAppId = "someAppId";
    String someCluster = "someCluster";
    String someCacheDir = "someCacheDir";
    String someZooMeta = "someZooMeta";

    System.setProperty(ZooClientSystemConsts.APP_ID, someAppId);
    System.setProperty(ConfigConsts.ZOO_CLUSTER_KEY, someCluster);
    System.setProperty(ZooClientSystemConsts.ZOO_CACHE_DIR, someCacheDir);
    System.setProperty(ConfigConsts.ZOO_META_KEY, someZooMeta);

    String anotherAppId = "anotherAppId";
    String anotherCluster = "anotherCluster";
    String anotherCacheDir = "anotherCacheDir";
    String anotherZooMeta = "anotherZooMeta";

    ConfigurableEnvironment environment = mock(ConfigurableEnvironment.class);

    when(environment.getProperty(ZooClientSystemConsts.APP_ID)).thenReturn(anotherAppId);
    when(environment.getProperty(ConfigConsts.ZOO_CLUSTER_KEY)).thenReturn(anotherCluster);
    when(environment.getProperty(ZooClientSystemConsts.ZOO_CACHE_DIR)).thenReturn(anotherCacheDir);
    when(environment.getProperty(ConfigConsts.ZOO_META_KEY)).thenReturn(anotherZooMeta);

    zooApplicationContextInitializer.initializeSystemProperty(environment);

    assertEquals(someAppId, System.getProperty(ZooClientSystemConsts.APP_ID));
    assertEquals(someCluster, System.getProperty(ConfigConsts.ZOO_CLUSTER_KEY));
    assertEquals(someCacheDir, System.getProperty(ZooClientSystemConsts.ZOO_CACHE_DIR));
    assertEquals(someZooMeta, System.getProperty(ConfigConsts.ZOO_META_KEY));
  }

  @Test
  public void testFillFromEnvironmentWithNoPropertyFromEnvironment() throws Exception {
    ConfigurableEnvironment environment = mock(ConfigurableEnvironment.class);

    zooApplicationContextInitializer.initializeSystemProperty(environment);

    assertNull(System.getProperty(ZooClientSystemConsts.APP_ID));
    assertNull(System.getProperty(ConfigConsts.ZOO_CLUSTER_KEY));
    assertNull(System.getProperty(ZooClientSystemConsts.ZOO_CACHE_DIR));
    assertNull(System.getProperty(ConfigConsts.ZOO_META_KEY));
  }

  @Test
  public void testPropertyNamesCacheEnabled() {
    ConfigurableEnvironment environment = mock(ConfigurableEnvironment.class);
    MutablePropertySources propertySources = new MutablePropertySources();
    when(environment.getPropertySources()).thenReturn(propertySources);
    when(environment.getProperty(PropertySourcesConstants.ZOO_BOOTSTRAP_NAMESPACES,
        ConfigConsts.NAMESPACE_APPLICATION)).thenReturn("");

    zooApplicationContextInitializer.initialize(environment);

    assertTrue(propertySources.contains(PropertySourcesConstants.ZOO_BOOTSTRAP_PROPERTY_SOURCE_NAME));
    assertFalse(propertySources.iterator().next() instanceof CachedCompositePropertySource);

    ConfigUtil configUtil = new ConfigUtil();
    configUtil = spy(configUtil);
    when(configUtil.isPropertyNamesCacheEnabled()).thenReturn(true);
    MockInjector.setInstance(ConfigUtil.class, configUtil);
    zooApplicationContextInitializer = new ZooApplicationContextInitializer();
    propertySources.remove(PropertySourcesConstants.ZOO_BOOTSTRAP_PROPERTY_SOURCE_NAME);

    zooApplicationContextInitializer.initialize(environment);

    assertTrue(propertySources.contains(PropertySourcesConstants.ZOO_BOOTSTRAP_PROPERTY_SOURCE_NAME));
    assertTrue(propertySources.iterator().next() instanceof CachedCompositePropertySource);
  }

  @Test
  public void testOverrideSystemProperties() {
    Properties properties = new Properties();
    properties.setProperty("server.port", "8080");
    ConfigurableEnvironment environment = mock(ConfigurableEnvironment.class);

    MutablePropertySources propertySources = new MutablePropertySources();
    propertySources.addLast(new PropertiesPropertySource(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, properties));

    when(environment.getPropertySources()).thenReturn(propertySources);
    when(environment.getProperty(PropertySourcesConstants.ZOO_BOOTSTRAP_NAMESPACES,
            ConfigConsts.NAMESPACE_APPLICATION)).thenReturn("");
    ConfigUtil configUtil = new ConfigUtil();
    configUtil = spy(configUtil);
    when(configUtil.isOverrideSystemProperties()).thenReturn(false);
    MockInjector.setInstance(ConfigUtil.class, configUtil);

    zooApplicationContextInitializer.initialize(environment);

    assertTrue(propertySources.contains(PropertySourcesConstants.ZOO_BOOTSTRAP_PROPERTY_SOURCE_NAME));
    assertEquals(propertySources.iterator().next().getName(), StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
  }
}
