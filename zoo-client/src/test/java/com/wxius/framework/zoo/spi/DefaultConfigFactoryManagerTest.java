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
package com.wxius.framework.zoo.spi;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wxius.framework.zoo.Datum;
//import com.wxius.framework.zoo.ConfigFile;
import com.wxius.framework.zoo.build.MockInjector;
//import com.wxius.framework.zoo.core.enums.ConfigFileFormat;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class DefaultConfigFactoryManagerTest {
  private DefaultDatumFactoryManager defaultConfigFactoryManager;

  @Before
  public void setUp() throws Exception {
    MockInjector.setInstance(DatumRegistry.class, new MockConfigRegistry());
    defaultConfigFactoryManager = new DefaultDatumFactoryManager();
  }

  @After
  public void tearDown() throws Exception {
    MockInjector.reset();
  }

  @Test
  public void testGetFactoryFromRegistry() throws Exception {
    DatumFactory result =
        defaultConfigFactoryManager.getFactory(MockConfigRegistry.NAMESPACE_REGISTERED);

    assertEquals(MockConfigRegistry.REGISTERED_CONFIGFACTORY, result);
  }

  @Test
  public void testGetFactoryFromNamespace() throws Exception {
    String someNamespace = "someName";
    MockInjector.setInstance(DatumFactory.class, someNamespace, new SomeConfigFactory());

    DatumFactory result = defaultConfigFactoryManager.getFactory(someNamespace);

    assertThat("When namespace is registered, should return the registerd config factory", result,
        instanceOf(SomeConfigFactory.class));
  }

  @Test
  public void testGetFactoryFromNamespaceMultipleTimes() throws Exception {
    String someNamespace = "someName";
    MockInjector.setInstance(DatumFactory.class, someNamespace, new SomeConfigFactory());

    DatumFactory result = defaultConfigFactoryManager.getFactory(someNamespace);
    DatumFactory anotherResult = defaultConfigFactoryManager.getFactory(someNamespace);

    assertThat(
        "Get config factory with the same namespace multiple times should returnt the same instance",
        anotherResult, equalTo(result));
  }

  @Test
  public void testGetFactoryFromDefault() throws Exception {
    String someNamespace = "someName";
    MockInjector.setInstance(DatumFactory.class, new AnotherConfigFactory());

    DatumFactory result = defaultConfigFactoryManager.getFactory(someNamespace);

    assertThat("When namespace is not registered, should return the default config factory", result,
        instanceOf(AnotherConfigFactory.class));
  }

  public static class MockConfigRegistry implements DatumRegistry {
    public static String NAMESPACE_REGISTERED = "some-namespace-registered";
    public static DatumFactory REGISTERED_CONFIGFACTORY = new DatumFactory() {
      @Override
      public Datum create(String namespace) {
        return null;
      }

//      @Override
//      public ConfigFile createConfigFile(String namespace, ConfigFileFormat configFileFormat) {
//        return null;
//      }

    };

    @Override
    public void register(String namespace, DatumFactory factory) {
      //do nothing
    }

    @Override
    public DatumFactory getFactory(String namespace) {
      if (namespace.equals(NAMESPACE_REGISTERED)) {
        return REGISTERED_CONFIGFACTORY;
      }
      return null;
    }
  }

  public static class SomeConfigFactory implements DatumFactory {
    @Override
    public Datum create(String namespace) {
      return null;
    }

//    @Override
//    public ConfigFile createConfigFile(String namespace, ConfigFileFormat configFileFormat) {
//      return null;
//    }
  }

  public static class AnotherConfigFactory implements DatumFactory {
    @Override
    public Datum create(String namespace) {
      return null;
    }

//    @Override
//    public ConfigFile createConfigFile(String namespace, ConfigFileFormat configFileFormat) {
//      return null;
//    }
  }

}
