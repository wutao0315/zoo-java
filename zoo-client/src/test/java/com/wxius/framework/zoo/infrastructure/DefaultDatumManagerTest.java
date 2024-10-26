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
package com.wxius.framework.zoo.infrastructure;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Properties;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wxius.framework.zoo.Datum;
import com.wxius.framework.zoo.build.MockInjector;
import com.wxius.framework.zoo.spi.DatumFactory;
import com.wxius.framework.zoo.spi.DatumFactoryManager;
import com.wxius.framework.zoo.util.ConfigUtil;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class DefaultDatumManagerTest {
  private DefaultDatumManager defaultConfigManager;
  private static String someConfigContent;

  @Before
  public void setUp() throws Exception {
    MockInjector.setInstance(DatumFactoryManager.class, new MockConfigFactoryManager());
    MockInjector.setInstance(ConfigUtil.class, new ConfigUtil());
    defaultConfigManager = new DefaultDatumManager();
    someConfigContent = "someContent";
  }

  @After
  public void tearDown() throws Exception {
    MockInjector.reset();
  }

  @Test
  public void testGetConfig() throws Exception {
    String someNamespace = "someName";
    String anotherNamespace = "anotherName";
    String someKey = "someKey";
    Datum config = defaultConfigManager.getDatum(someNamespace);
    Datum anotherConfig = defaultConfigManager.getDatum(anotherNamespace);

    assertEquals(someNamespace + ":" + someKey, config.getProperty(someKey, null));
    assertEquals(anotherNamespace + ":" + someKey, anotherConfig.getProperty(someKey, null));
  }

  @Test
  public void testGetConfigMultipleTimesWithSameNamespace() throws Exception {
    String someNamespace = "someName";
    Datum config = defaultConfigManager.getDatum(someNamespace);
    Datum anotherConfig = defaultConfigManager.getDatum(someNamespace);

    assertThat(
        "Get config multiple times with the same namespace should return the same config instance",
        config, equalTo(anotherConfig));
  }

  @Test
  public void testGetConfigFile() throws Exception {
    String someNamespace = "someName";
//    ConfigFileFormat someConfigFileFormat = ConfigFileFormat.Properties;

//    ConfigFile configFile =
//        defaultConfigManager.getConfigFile(someNamespace, someConfigFileFormat);
//
//    assertEquals(someConfigFileFormat, configFile.getConfigFileFormat());
//    assertEquals(someConfigContent, configFile.getContent());
  }

  @Test
  public void testGetConfigFileMultipleTimesWithSameNamespace() throws Exception {
    String someNamespace = "someName";
//    ConfigFileFormat someConfigFileFormat = ConfigFileFormat.Properties;
//
//    ConfigFile someConfigFile =
//        defaultConfigManager.getConfigFile(someNamespace, someConfigFileFormat);
//    ConfigFile anotherConfigFile =
//        defaultConfigManager.getConfigFile(someNamespace, someConfigFileFormat);
//
//    assertThat(
//        "Get config file multiple times with the same namespace should return the same config file instance",
//        someConfigFile, equalTo(anotherConfigFile));

  }

  public static class MockConfigFactoryManager implements DatumFactoryManager {

    @Override
    public DatumFactory getFactory(String namespace) {
      return new DatumFactory() {
        @Override
        public Datum create(final String namespace) {
          return new AbstractDatum() {
            @Override
            public String getProperty(String key, String defaultValue) {
              return namespace + ":" + key;
            }

            @Override
            public Set<String> getPropertyNames() {
              return null;
            }

//            @Override
//            public ConfigSourceType getSourceType() {
//              return null;
//            }
          };
        }

//        @Override
//        public ConfigFile createConfigFile(String namespace, final ConfigFileFormat configFileFormat) {
//          DatumRepository someConfigRepository = mock(DatumRepository.class);
//          return new AbstractConfigFile(namespace, someConfigRepository) {
//
//            @Override
//            protected void update(Properties newProperties) {
//
//            }
//
//            @Override
//            public String getContent() {
//              return someConfigContent;
//            }
//
//            @Override
//            public boolean hasContent() {
//              return true;
//            }
//
//            @Override
//            public ConfigFileFormat getConfigFileFormat() {
//              return configFileFormat;
//            }
//          };
//        }
      };
    }
  }
}
