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
package com.wxius.framework.zoo.config.data.importer;

import com.wxius.framework.zoo.Datum;
import com.wxius.framework.zoo.build.ZooInjector;
import com.wxius.framework.zoo.config.data.injector.ZooMockInjectorCustomizer;
import com.wxius.framework.zoo.config.data.internals.PureZooConfigFactory;
import com.wxius.framework.zoo.spi.DatumFactory;
import com.wxius.framework.zoo.spi.DefaultDatumFactory;
import com.github.stefanbirkner.systemlambda.SystemLambda;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class PureZooConfigTest {

  @Before
  public void before() {
    System.setProperty("env", "local");
  }

  @After
  public void after() {
    System.clearProperty("spring.profiles.active");
    System.clearProperty("env");
    ZooMockInjectorCustomizer.clear();
  }

  @Test
  public void testDefaultConfigWithSystemProperties() {
    System.setProperty("spring.profiles.active", "test");
    ZooMockInjectorCustomizer.register(DatumFactory.class,
        DefaultDatumFactory::new);
    DatumFactory configFactory = ZooInjector.getInstance(DatumFactory.class);
    Datum config = configFactory.create("application");
    Assert.assertEquals("test", config.getProperty("spring.profiles.active", null));
  }

  @Test
  public void testPureZooConfigWithSystemProperties() {
    System.setProperty("spring.profiles.active", "test");
    ZooMockInjectorCustomizer.register(DatumFactory.class,
        PureZooConfigFactory::new);
    DatumFactory configFactory = ZooInjector.getInstance(DatumFactory.class);
    Datum config = configFactory.create("application");
    Assert.assertNull(config.getProperty("spring.profiles.active", null));
  }

  @Test
  public void testDefaultConfigWithEnvironmentVariables() throws Exception {
    SystemLambda.withEnvironmentVariable(
        "SPRING_PROFILES_ACTIVE",
        "test-env")
        .execute(() -> {
          ZooMockInjectorCustomizer.register(DatumFactory.class,
              DefaultDatumFactory::new);
          DatumFactory configFactory = ZooInjector.getInstance(DatumFactory.class);
          Datum config = configFactory.create("application");
          Assert.assertEquals("test-env", config.getProperty("SPRING_PROFILES_ACTIVE", null));
        });
  }

  @Test
  public void testPureZooConfigWithEnvironmentVariables() throws Exception {
    SystemLambda.withEnvironmentVariable(
        "SPRING_PROFILES_ACTIVE",
        "test-env")
        .execute(() -> {
          ZooMockInjectorCustomizer.register(DatumFactory.class,
              PureZooConfigFactory::new);
          DatumFactory configFactory = ZooInjector.getInstance(DatumFactory.class);
          Datum config = configFactory.create("application");
          Assert.assertNull(config.getProperty("SPRING_PROFILES_ACTIVE", null));
        });
  }
}
