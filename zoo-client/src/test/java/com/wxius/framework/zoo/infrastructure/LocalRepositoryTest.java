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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//import com.wxius.framework.zoo.enums.ConfigSourceType;
import com.wxius.framework.zoo.util.factory.PropertiesFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.wxius.framework.zoo.build.MockInjector;
import com.wxius.framework.zoo.core.ConfigConsts;
import com.wxius.framework.zoo.util.ConfigUtil;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Files;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Created by Jason on 4/9/16.
 */
public class LocalRepositoryTest {
  private File someBaseDir;
  private String someNamespace;
  private HttpRepository upstreamRepo;
  private Properties someProperties;
  private static String someAppId = "someApp";
  private static String someCluster = "someCluster";
  private String defaultKey;
  private String defaultValue;
//  private ConfigSourceType someSourceType;

  @Before
  public void setUp() throws Exception {
    someBaseDir = new File("src/test/resources/config-cache");
    someBaseDir.mkdir();

    someNamespace = "someName";
    someProperties = new Properties();
    defaultKey = "defaultKey";
    defaultValue = "defaultValue";
    someProperties.setProperty(defaultKey, defaultValue);
//    someSourceType = ConfigSourceType.REMOTE;
    upstreamRepo = mock(HttpRepository.class);
    when(upstreamRepo.getDatum()).thenReturn(someProperties);
    //when(upstreamRepo.getSourceType()).thenReturn(someSourceType);

    MockInjector.setInstance(ConfigUtil.class, new MockConfigUtil());
    PropertiesFactory propertiesFactory = mock(PropertiesFactory.class);
    when(propertiesFactory.getPropertiesInstance()).thenAnswer(new Answer<Properties>() {
      @Override
      public Properties answer(InvocationOnMock invocation) {
        return new Properties();
      }
    });
    MockInjector.setInstance(PropertiesFactory.class, propertiesFactory);
  }

  @After
  public void tearDown() throws Exception {
    MockInjector.reset();
    recursiveDelete(someBaseDir);
  }

  //helper method to clean created files
  private void recursiveDelete(File file) {
    if (!file.exists()) {
      return;
    }
    if (file.isDirectory()) {
      for (File f : file.listFiles()) {
        recursiveDelete(f);
      }
    }
    file.delete();
  }

  private String assembleLocalCacheFileName() {
    return String.format("%s.properties", Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR)
        .join(someAppId, someCluster, someNamespace));
  }


  @Test
  public void testLoadConfigWithLocalFile() throws Exception {

    String someKey = "someKey";
    String someValue = "someValue\nxxx\nyyy";

    Properties someProperties = new Properties();
    someProperties.setProperty(someKey, someValue);
    createLocalCachePropertyFile(someProperties);

//    LocalRepository localRepo = new LocalRepository(someNamespace);
//    localRepo.setLocalCacheDir(someBaseDir, true);
//    Properties properties = localRepo.getDatum();
//
//    assertEquals(someValue, properties.getProperty(someKey));
    //assertEquals(ConfigSourceType.LOCAL, localRepo.getSourceType());
  }

  @Test
  public void testLoadConfigWithLocalFileAndFallbackRepo() throws Exception {
    File file = new File(someBaseDir, assembleLocalCacheFileName());

    String someValue = "someValue";

    Files.write(defaultKey + "=" + someValue, file, Charsets.UTF_8);

    LocalRepository localRepo = new LocalRepository(someNamespace, upstreamRepo);
    localRepo.setLocalCacheDir(someBaseDir, true);

    Properties properties = localRepo.getDatum();

    assertEquals(defaultValue, properties.getProperty(defaultKey));
    //assertEquals(someSourceType, localRepo.getSourceType());
  }

  @Test
  public void testLoadConfigWithNoLocalFile() throws Exception {
    LocalRepository localFileConfigRepository =
        new LocalRepository(someNamespace, upstreamRepo);
    localFileConfigRepository.setLocalCacheDir(someBaseDir, true);

    Properties result = localFileConfigRepository.getDatum();

    assertEquals(
        "LocalFileConfigRepository's properties should be the same as fallback repo's when there is no local cache",
        result, someProperties);
    //assertEquals(someSourceType, localFileConfigRepository.getSourceType());
  }

  @Test
  public void testLoadConfigWithNoLocalFileMultipleTimes() throws Exception {
    LocalRepository localRepo =
        new LocalRepository(someNamespace, upstreamRepo);
    localRepo.setLocalCacheDir(someBaseDir, true);

    Properties someProperties = localRepo.getDatum();

//    LocalRepository
//        anotherLocalRepoWithNoFallback =
//        new LocalRepository(someNamespace);
//    anotherLocalRepoWithNoFallback.setLocalCacheDir(someBaseDir, true);

//    Properties anotherProperties = anotherLocalRepoWithNoFallback.getDatum();

//    assertEquals(
//        "LocalFileConfigRepository should persist local cache files and return that afterwards",
//        someProperties, anotherProperties);
    //assertEquals(someSourceType, localRepo.getSourceType());
  }

  @Test
  public void testOnRepositoryChange() throws Exception {
    RepositoryChangeListener someListener = mock(RepositoryChangeListener.class);

    LocalRepository localFileConfigRepository =
        new LocalRepository(someNamespace, upstreamRepo);

    //assertEquals(someSourceType, localFileConfigRepository.getSourceType());

    localFileConfigRepository.setLocalCacheDir(someBaseDir, true);
    localFileConfigRepository.addChangeListener(someListener);

    localFileConfigRepository.getDatum();

    Properties anotherProperties = new Properties();
    anotherProperties.put("anotherKey", "anotherValue");

//    ConfigSourceType anotherSourceType = ConfigSourceType.NONE;
    //when(upstreamRepo.getSourceType()).thenReturn(anotherSourceType);

    localFileConfigRepository.onRepositoryChange(someNamespace, anotherProperties);

    final ArgumentCaptor<Properties> captor = ArgumentCaptor.forClass(Properties.class);

    verify(someListener, times(1)).onRepositoryChange(eq(someNamespace), captor.capture());

    assertEquals(anotherProperties, captor.getValue());
    //assertEquals(anotherSourceType, localFileConfigRepository.getSourceType());
  }

  public static class MockConfigUtil extends ConfigUtil {
    @Override
    public String getAppId() {
      return someAppId;
    }

    @Override
    public String getCluster() {
      return someCluster;
    }
  }

  private File createLocalCachePropertyFile(Properties properties) throws IOException {
    File file = new File(someBaseDir, assembleLocalCacheFileName());
    FileOutputStream in = null;
    try {
      in = new FileOutputStream(file);
      properties.store(in, "Persisted by LocalFileConfigRepositoryTest");
    } finally {
      if (in != null) {
        in.close();
      }
    }
    return file;
  }
}
