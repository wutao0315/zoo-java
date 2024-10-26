///*
// * Copyright 2022 Zoo Authors
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// *
// */
//package com.wxius.framework.zoo.infrastructure;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertNull;
//import static org.junit.Assert.assertTrue;
//import static org.mockito.Mockito.when;
//
//import com.wxius.framework.zoo.ConfigFileChangeListener;
//import com.wxius.framework.zoo.build.MockInjector;
//import com.wxius.framework.zoo.enums.PropertyChangeType;
//import com.wxius.framework.zoo.model.ConfigFileChangeEvent;
//import com.wxius.framework.zoo.util.factory.PropertiesFactory;
//import com.google.common.util.concurrent.SettableFuture;
//import java.util.Properties;
//
//import java.util.concurrent.TimeUnit;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.mockito.invocation.InvocationOnMock;
//import org.mockito.junit.MockitoJUnitRunner;
//
//import com.wxius.framework.zoo.core.ConfigConsts;
//import com.wxius.framework.zoo.core.enums.ConfigFileFormat;
//import org.mockito.stubbing.Answer;
//
///**
// * @author Jason Song(song_s@ctrip.com)
// */
//@RunWith(MockitoJUnitRunner.class)
//public class XmlConfigFileTest {
//
//  private String someNamespace;
//  @Mock
//  private DatumRepository configRepository;
//  @Mock
//  private PropertiesFactory propertiesFactory;
//
//  @Before
//  public void setUp() throws Exception {
//    someNamespace = "someName";
//
//    when(propertiesFactory.getPropertiesInstance()).thenAnswer(new Answer<Properties>() {
//      @Override
//      public Properties answer(InvocationOnMock invocation) {
//        return new Properties();
//      }
//    });
//    MockInjector.setInstance(PropertiesFactory.class, propertiesFactory);
//  }
//
//  @After
//  public void tearDown() throws Exception {
//    MockInjector.reset();
//    System.clearProperty(PropertiesFactory.ZOO_PROPERTY_ORDER_ENABLE);
//  }
//
//  @Test
//  public void testWhenHasContent() throws Exception {
//    Properties someProperties = new Properties();
//    String key = ConfigConsts.CONFIG_FILE_CONTENT_KEY;
//    String someValue = "someValue";
//    someProperties.setProperty(key, someValue);
//
//    when(configRepository.getDatum()).thenReturn(someProperties);
//
//    XmlConfigFile configFile = new XmlConfigFile(someNamespace, configRepository);
//
//    assertEquals(ConfigFileFormat.XML, configFile.getConfigFileFormat());
//    assertEquals(someNamespace, configFile.getNamespace());
//    assertTrue(configFile.hasContent());
//    assertEquals(someValue, configFile.getContent());
//  }
//
//  @Test
//  public void testWhenHasNoContent() throws Exception {
//    when(configRepository.getDatum()).thenReturn(null);
//
//    XmlConfigFile configFile = new XmlConfigFile(someNamespace, configRepository);
//
//    assertFalse(configFile.hasContent());
//    assertNull(configFile.getContent());
//  }
//
//  @Test
//  public void testWhenConfigRepositoryHasError() throws Exception {
//    when(configRepository.getDatum()).thenThrow(new RuntimeException("someError"));
//
//    XmlConfigFile configFile = new XmlConfigFile(someNamespace, configRepository);
//
//    assertFalse(configFile.hasContent());
//    assertNull(configFile.getContent());
//  }
//
//  @Test
//  public void testOnRepositoryChange() throws Exception {
//    Properties someProperties = new Properties();
//    String key = ConfigConsts.CONFIG_FILE_CONTENT_KEY;
//    String someValue = "someValue";
//    String anotherValue = "anotherValue";
//    someProperties.setProperty(key, someValue);
//
//    when(configRepository.getDatum()).thenReturn(someProperties);
//
//    XmlConfigFile configFile = new XmlConfigFile(someNamespace, configRepository);
//
//    assertEquals(someValue, configFile.getContent());
//
//    Properties anotherProperties = new Properties();
//    anotherProperties.setProperty(key, anotherValue);
//
//    final SettableFuture<ConfigFileChangeEvent> configFileChangeFuture = SettableFuture.create();
//    ConfigFileChangeListener someListener = new ConfigFileChangeListener() {
//      @Override
//      public void onChange(ConfigFileChangeEvent changeEvent) {
//        configFileChangeFuture.set(changeEvent);
//      }
//    };
//
//    configFile.addChangeListener(someListener);
//
//    configFile.onRepositoryChange(someNamespace, anotherProperties);
//
//    ConfigFileChangeEvent changeEvent = configFileChangeFuture.get(500, TimeUnit.MILLISECONDS);
//
//    assertEquals(anotherValue, configFile.getContent());
//    assertEquals(someNamespace, changeEvent.getNamespace());
//    assertEquals(someValue, changeEvent.getOldValue());
//    assertEquals(anotherValue, changeEvent.getNewValue());
//    assertEquals(PropertyChangeType.MODIFIED, changeEvent.getChangeType());
//  }
//
//  @Test
//  public void testOnRepositoryChangeWithContentAdded() throws Exception {
//    Properties someProperties = new Properties();
//    String key = ConfigConsts.CONFIG_FILE_CONTENT_KEY;
//    String someValue = "someValue";
//
//    when(configRepository.getDatum()).thenReturn(someProperties);
//
//    XmlConfigFile configFile = new XmlConfigFile(someNamespace, configRepository);
//
//    assertEquals(null, configFile.getContent());
//
//    Properties anotherProperties = new Properties();
//    anotherProperties.setProperty(key, someValue);
//
//    final SettableFuture<ConfigFileChangeEvent> configFileChangeFuture = SettableFuture.create();
//    ConfigFileChangeListener someListener = new ConfigFileChangeListener() {
//      @Override
//      public void onChange(ConfigFileChangeEvent changeEvent) {
//        configFileChangeFuture.set(changeEvent);
//      }
//    };
//
//    configFile.addChangeListener(someListener);
//
//    configFile.onRepositoryChange(someNamespace, anotherProperties);
//
//    ConfigFileChangeEvent changeEvent = configFileChangeFuture.get(500, TimeUnit.MILLISECONDS);
//
//    assertEquals(someValue, configFile.getContent());
//    assertEquals(someNamespace, changeEvent.getNamespace());
//    assertEquals(null, changeEvent.getOldValue());
//    assertEquals(someValue, changeEvent.getNewValue());
//    assertEquals(PropertyChangeType.ADDED, changeEvent.getChangeType());
//  }
//
//  @Test
//  public void testOnRepositoryChangeWithContentDeleted() throws Exception {
//    Properties someProperties = new Properties();
//    String key = ConfigConsts.CONFIG_FILE_CONTENT_KEY;
//    String someValue = "someValue";
//    someProperties.setProperty(key, someValue);
//
//    when(configRepository.getDatum()).thenReturn(someProperties);
//
//    XmlConfigFile configFile = new XmlConfigFile(someNamespace, configRepository);
//
//    assertEquals(someValue, configFile.getContent());
//
//    Properties anotherProperties = new Properties();
//
//    final SettableFuture<ConfigFileChangeEvent> configFileChangeFuture = SettableFuture.create();
//    ConfigFileChangeListener someListener = new ConfigFileChangeListener() {
//      @Override
//      public void onChange(ConfigFileChangeEvent changeEvent) {
//        configFileChangeFuture.set(changeEvent);
//      }
//    };
//
//    configFile.addChangeListener(someListener);
//
//    configFile.onRepositoryChange(someNamespace, anotherProperties);
//
//    ConfigFileChangeEvent changeEvent = configFileChangeFuture.get(500, TimeUnit.MILLISECONDS);
//
//    assertEquals(null, configFile.getContent());
//    assertEquals(someNamespace, changeEvent.getNamespace());
//    assertEquals(someValue, changeEvent.getOldValue());
//    assertEquals(null, changeEvent.getNewValue());
//    assertEquals(PropertyChangeType.DELETED, changeEvent.getChangeType());
//  }
//
//  @Test
//  public void testWhenConfigRepositoryHasErrorAndThenRecovered() throws Exception {
//    Properties someProperties = new Properties();
//    String key = ConfigConsts.CONFIG_FILE_CONTENT_KEY;
//    String someValue = "someValue";
//    someProperties.setProperty(key, someValue);
//
//    when(configRepository.getDatum()).thenThrow(new RuntimeException("someError"));
//
//    XmlConfigFile configFile = new XmlConfigFile(someNamespace, configRepository);
//
//    assertFalse(configFile.hasContent());
//    assertNull(configFile.getContent());
//
//    configFile.onRepositoryChange(someNamespace, someProperties);
//
//    assertTrue(configFile.hasContent());
//    assertEquals(someValue, configFile.getContent());
//  }
//}
