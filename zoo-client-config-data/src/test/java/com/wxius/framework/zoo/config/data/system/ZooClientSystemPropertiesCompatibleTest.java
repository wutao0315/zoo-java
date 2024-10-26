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
package com.wxius.framework.zoo.config.data.system;

import com.wxius.framework.zoo.core.ZooClientSystemConsts;
import com.wxius.framework.zoo.spring.boot.ZooApplicationContextInitializer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ZooClientPropertyCompatibleTestConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ZooClientSystemPropertiesCompatibleTest {

  @Autowired
  private ConfigurableEnvironment environment;

  @Test
  public void testSystemPropertiesCompatible() {
    System.setProperty(ZooClientSystemConsts.DEPRECATED_ZOO_CACHE_DIR, "test-3/cacheDir");
    System
        .setProperty(ZooClientSystemConsts.DEPRECATED_ZOO_ACCESS_KEY_SECRET, "test-3-secret");
    System.setProperty(ZooClientSystemConsts.DEPRECATED_ZOO_CONFIG_SERVICE,
        "https://test-3-config-service");

    Assert.assertEquals("test-3/cacheDir",
        this.environment.getProperty(ZooClientSystemConsts.ZOO_CACHE_DIR));
    Assert.assertEquals("test-3-secret",
        this.environment.getProperty(ZooClientSystemConsts.ZOO_ACCESS_KEY_SECRET));
    Assert.assertEquals("https://test-3-config-service",
        this.environment.getProperty(ZooClientSystemConsts.ZOO_CONFIG_SERVICE));

    System.clearProperty(ZooClientSystemConsts.DEPRECATED_ZOO_CACHE_DIR);
    System.clearProperty(ZooClientSystemConsts.DEPRECATED_ZOO_ACCESS_KEY_SECRET);
    System.clearProperty(ZooClientSystemConsts.DEPRECATED_ZOO_CONFIG_SERVICE);
  }

  @After
  public void clearProperty() {
    for (String propertyName : ZooApplicationContextInitializer.ZOO_SYSTEM_PROPERTIES) {
      System.clearProperty(propertyName);
    }
  }
}
