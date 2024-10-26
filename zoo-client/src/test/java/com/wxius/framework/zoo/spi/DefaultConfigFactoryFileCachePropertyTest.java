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

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wxius.framework.zoo.build.MockInjector;
import com.wxius.framework.zoo.infrastructure.DatumRepository;
import com.wxius.framework.zoo.infrastructure.LocalRepository;
import com.wxius.framework.zoo.infrastructure.HttpRepository;
import com.wxius.framework.zoo.util.ConfigUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DefaultConfigFactoryFileCachePropertyTest {

  private DefaultDatumFactory configFactory;
  private ConfigUtil someConfigUtil;
  private String someNamespace;

  @Before
  public void setUp() throws Exception {
    someNamespace = "someNamespace";
    someConfigUtil = mock(ConfigUtil.class);
    MockInjector.setInstance(ConfigUtil.class, someConfigUtil);
    configFactory = spy(new DefaultDatumFactory());
  }

  @Test
  public void testCreateFileEnableConfigRepository() throws Exception {
    LocalRepository someLocalConfigRepository = mock(LocalRepository.class);
//    when(someConfigUtil.isPropertyFileCacheEnabled()).thenReturn(true);
//    doReturn(someLocalConfigRepository).when(configFactory)
//        .createLocalConfigRepository(someNamespace);
//    DatumRepository configRepository = configFactory.createConfigRepository(someNamespace);
//    assertSame(someLocalConfigRepository, configRepository);
//    verify(configFactory, times(1)).createLocalConfigRepository(someNamespace);
//    verify(configFactory, never()).createRemoteConfigRepository(someNamespace);
  }

  @Test
  public void testCreateFileDisableConfigRepository() throws Exception {
    HttpRepository someRemoteConfigRepository = mock(HttpRepository.class);
//    when(someConfigUtil.isPropertyFileCacheEnabled()).thenReturn(false);
//    doReturn(someRemoteConfigRepository).when(configFactory)
//        .createRemoteConfigRepository(someNamespace);
//    DatumRepository configRepository = configFactory.createConfigRepository(someNamespace);
//    assertSame(someRemoteConfigRepository, configRepository);
//    verify(configFactory, never()).createLocalConfigRepository(someNamespace);
//    verify(configFactory, times(1)).createRemoteConfigRepository(someNamespace);
  }

  @After
  public void tearDown() throws Exception {
    MockInjector.reset();
  }
}
