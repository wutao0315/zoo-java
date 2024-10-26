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
package com.wxius.framework.zoo.util;

import com.wxius.framework.zoo.core.ConfigConsts;

import com.wxius.framework.zoo.core.ZooClientSystemConsts;
import com.wxius.framework.zoo.util.factory.PropertiesFactory;
import java.io.File;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigUtilTest {

  @After
  public void tearDown() throws Exception {
    System.clearProperty(ConfigConsts.ZOO_CLUSTER_KEY);
    System.clearProperty("zoo.connectTimeout");
    System.clearProperty("zoo.readTimeout");
    System.clearProperty("zoo.refreshInterval");
    System.clearProperty("zoo.loadConfigQPS");
    System.clearProperty("zoo.longPollQPS");
    System.clearProperty("zoo.configCacheSize");
    System.clearProperty("zoo.longPollingInitialDelayInMills");
    System.clearProperty("zoo.autoUpdateInjectedSpringProperties");
    System.clearProperty(ZooClientSystemConsts.ZOO_CACHE_DIR);
    System.clearProperty(PropertiesFactory.ZOO_PROPERTY_ORDER_ENABLE);
    System.clearProperty(ZooClientSystemConsts.ZOO_PROPERTY_NAMES_CACHE_ENABLE);
  }

  @Test
  public void testZooCluster() throws Exception {
    String someCluster = "someCluster";
    System.setProperty(ConfigConsts.ZOO_CLUSTER_KEY, someCluster);

    ConfigUtil configUtil = new ConfigUtil();

    assertEquals(someCluster, configUtil.getCluster());
  }

  @Test
  public void testCustomizeConnectTimeout() throws Exception {
    int someConnectTimeout = 1;
    System.setProperty("zoo.connectTimeout", String.valueOf(someConnectTimeout));

    ConfigUtil configUtil = new ConfigUtil();

    assertEquals(someConnectTimeout, configUtil.getConnectTimeout());
  }

  @Test
  public void testCustomizeInvalidConnectTimeout() throws Exception {
    String someInvalidConnectTimeout = "a";
    System.setProperty("zoo.connectTimeout", someInvalidConnectTimeout);

    ConfigUtil configUtil = new ConfigUtil();

    assertTrue(configUtil.getConnectTimeout() > 0);
  }

  @Test
  public void testCustomizeReadTimeout() throws Exception {
    int someReadTimeout = 1;
    System.setProperty("zoo.readTimeout", String.valueOf(someReadTimeout));

    ConfigUtil configUtil = new ConfigUtil();

    assertEquals(someReadTimeout, configUtil.getReadTimeout());
  }

  @Test
  public void testCustomizeInvalidReadTimeout() throws Exception {
    String someInvalidReadTimeout = "a";
    System.setProperty("zoo.readTimeout", someInvalidReadTimeout);

    ConfigUtil configUtil = new ConfigUtil();

    assertTrue(configUtil.getReadTimeout() > 0);
  }

  @Test
  public void testCustomizeRefreshInterval() throws Exception {
    int someRefreshInterval = 1;
    System.setProperty("zoo.refreshInterval", String.valueOf(someRefreshInterval));

    ConfigUtil configUtil = new ConfigUtil();

    assertEquals(someRefreshInterval, configUtil.getRefreshInterval());
  }

  @Test
  public void testCustomizeInvalidRefreshInterval() throws Exception {
    String someInvalidRefreshInterval = "a";
    System.setProperty("zoo.refreshInterval", someInvalidRefreshInterval);

    ConfigUtil configUtil = new ConfigUtil();

    assertTrue(configUtil.getRefreshInterval() > 0);
  }

  @Test
  public void testCustomizeLoadConfigQPS() throws Exception {
    int someQPS = 1;
    System.setProperty("zoo.loadConfigQPS", String.valueOf(someQPS));

    ConfigUtil configUtil = new ConfigUtil();

    assertEquals(someQPS, configUtil.getLoadConfigQPS());
  }

  @Test
  public void testCustomizeInvalidLoadConfigQPS() throws Exception {
    String someInvalidQPS = "a";
    System.setProperty("zoo.loadConfigQPS", someInvalidQPS);

    ConfigUtil configUtil = new ConfigUtil();

    assertTrue(configUtil.getLoadConfigQPS() > 0);
  }

  @Test
  public void testCustomizeLongPollQPS() throws Exception {
    int someQPS = 1;
    System.setProperty("zoo.longPollQPS", String.valueOf(someQPS));

    ConfigUtil configUtil = new ConfigUtil();

    assertEquals(someQPS, configUtil.getLongPollQPS());
  }

  @Test
  public void testCustomizeInvalidLongPollQPS() throws Exception {
    String someInvalidQPS = "a";
    System.setProperty("zoo.longPollQPS", someInvalidQPS);

    ConfigUtil configUtil = new ConfigUtil();

    assertTrue(configUtil.getLongPollQPS() > 0);
  }

  @Test
  public void testCustomizeMaxConfigCacheSize() throws Exception {
    long someCacheSize = 1;
    System.setProperty("zoo.configCacheSize", String.valueOf(someCacheSize));

    ConfigUtil configUtil = new ConfigUtil();

    assertEquals(someCacheSize, configUtil.getMaxConfigCacheSize());
  }

  @Test
  public void testCustomizeInvalidMaxConfigCacheSize() throws Exception {
    String someInvalidCacheSize = "a";
    System.setProperty("zoo.configCacheSize", someInvalidCacheSize);

    ConfigUtil configUtil = new ConfigUtil();

    assertTrue(configUtil.getMaxConfigCacheSize() > 0);
  }

  @Test
  public void testCustomizeLongPollingInitialDelayInMills() throws Exception {
    long someLongPollingDelayInMills = 1;
    System.setProperty("zoo.longPollingInitialDelayInMills",
        String.valueOf(someLongPollingDelayInMills));

    ConfigUtil configUtil = new ConfigUtil();

    assertEquals(someLongPollingDelayInMills, configUtil.getLongPollingInitialDelayInMills());
  }

  @Test
  public void testCustomizeInvalidLongPollingInitialDelayInMills() throws Exception {
    String someInvalidLongPollingDelayInMills = "a";
    System.setProperty("zoo.longPollingInitialDelayInMills", someInvalidLongPollingDelayInMills);

    ConfigUtil configUtil = new ConfigUtil();

    assertTrue(configUtil.getLongPollingInitialDelayInMills() > 0);
  }

  @Test
  public void testCustomizeAutoUpdateInjectedSpringProperties() throws Exception {
    boolean someAutoUpdateInjectedSpringProperties = false;
    System.setProperty("zoo.autoUpdateInjectedSpringProperties",
        String.valueOf(someAutoUpdateInjectedSpringProperties));

    ConfigUtil configUtil = new ConfigUtil();

    assertEquals(someAutoUpdateInjectedSpringProperties,
        configUtil.isAutoUpdateInjectedSpringPropertiesEnabled());
  }

  @Test
  public void testLocalCacheDirWithSystemProperty() throws Exception {
    String someCacheDir = "someCacheDir";
    String someAppId = "someAppId";

    System.setProperty(ZooClientSystemConsts.ZOO_CACHE_DIR, someCacheDir);

    ConfigUtil configUtil = spy(new ConfigUtil());

    doReturn(someAppId).when(configUtil).getAppId();

    assertEquals(someCacheDir + File.separator + someAppId, configUtil.getDefaultLocalCacheDir());
  }

  @Test
  public void testDefaultLocalCacheDir() throws Exception {
    String someAppId = "someAppId";

    ConfigUtil configUtil = spy(new ConfigUtil());

    doReturn(someAppId).when(configUtil).getAppId();

    doReturn(true).when(configUtil).isOSWindows();

    assertEquals("C:\\opt\\data\\" + someAppId, configUtil.getDefaultLocalCacheDir());

    doReturn(false).when(configUtil).isOSWindows();

    assertEquals("/opt/data/" + someAppId, configUtil.getDefaultLocalCacheDir());
  }

  @Test
  public void testCustomizePropertiesOrdered() {
    boolean propertiesOrdered = true;
    System.setProperty(PropertiesFactory.ZOO_PROPERTY_ORDER_ENABLE,
        String.valueOf(propertiesOrdered));

    ConfigUtil configUtil = new ConfigUtil();

    assertEquals(propertiesOrdered,
        configUtil.isPropertiesOrderEnabled());
  }

  @Test
  public void test() {
    ConfigUtil configUtil = new ConfigUtil();
    assertFalse(configUtil.isPropertyNamesCacheEnabled());

    System.setProperty(ZooClientSystemConsts.ZOO_PROPERTY_NAMES_CACHE_ENABLE, "true");
    configUtil = new ConfigUtil();
    assertTrue(configUtil.isPropertyNamesCacheEnabled());
  }
}
