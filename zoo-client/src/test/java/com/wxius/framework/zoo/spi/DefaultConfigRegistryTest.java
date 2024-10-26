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
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.wxius.framework.zoo.Datum;
//import com.wxius.framework.zoo.ConfigFile;
//import com.wxius.framework.zoo.core.enums.ConfigFileFormat;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class DefaultConfigRegistryTest {
  private DefaultDatumRegistry defaultConfigRegistry;

  @Before
  public void setUp() throws Exception {
    defaultConfigRegistry = new DefaultDatumRegistry();
  }

  @Test
  public void testGetFactory() throws Exception {
    String someNamespace = "someName";
    DatumFactory someConfigFactory = new MockConfigFactory();

    defaultConfigRegistry.register(someNamespace, someConfigFactory);

    assertThat("Should return the registered config factory",
        defaultConfigRegistry.getFactory(someNamespace), equalTo(someConfigFactory));
  }

  @Test
  public void testGetFactoryWithNamespaceUnregistered() throws Exception {
    String someUnregisteredNamespace = "someName";

    assertNull(defaultConfigRegistry.getFactory(someUnregisteredNamespace));
  }

  public static class MockConfigFactory implements DatumFactory {

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
