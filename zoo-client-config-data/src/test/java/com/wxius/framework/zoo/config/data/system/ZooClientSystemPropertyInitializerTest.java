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

import com.wxius.framework.zoo.spring.boot.ZooApplicationContextInitializer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class ZooClientSystemPropertyInitializerTest {

  @Test
  public void testSystemPropertyNames() {
    for (String propertyName : ZooApplicationContextInitializer.ZOO_SYSTEM_PROPERTIES) {
      Assert.assertTrue(ConfigurationPropertyName.isValid(propertyName));
    }
  }

  @Test
  public void testInitializeSystemProperty() {
    Map<String, String> map = new LinkedHashMap<>();
    for (String propertyName : ZooApplicationContextInitializer.ZOO_SYSTEM_PROPERTIES) {
      System.clearProperty(propertyName);
      map.put(propertyName, String.valueOf(ThreadLocalRandom.current().nextLong()));
    }
    MapConfigurationPropertySource propertySource = new MapConfigurationPropertySource(map);
    Binder binder = new Binder(propertySource);
    ZooClientSystemPropertyInitializer initializer = new ZooClientSystemPropertyInitializer(
        Supplier::get);
    initializer.initializeSystemProperty(binder, null);
    for (String propertyName : ZooApplicationContextInitializer.ZOO_SYSTEM_PROPERTIES) {
      Assert.assertEquals(map.get(propertyName), System.getProperty(propertyName));
    }
  }

  @After
  public void clearProperty() {
    for (String propertyName : ZooApplicationContextInitializer.ZOO_SYSTEM_PROPERTIES) {
      System.clearProperty(propertyName);
    }
  }
}
