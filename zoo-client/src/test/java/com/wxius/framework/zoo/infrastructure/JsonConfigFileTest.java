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
////import com.wxius.framework.zoo.enums.ConfigSourceType;
//import java.util.Properties;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.mockito.junit.MockitoJUnitRunner;
//
//import com.wxius.framework.zoo.core.ConfigConsts;
////import com.wxius.framework.zoo.core.enums.ConfigFileFormat;
//
///**
// * @author Jason Song(song_s@ctrip.com)
// */
//@RunWith(MockitoJUnitRunner.class)
//public class JsonConfigFileTest {
//  private String someNamespace;
//  @Mock
//  private DatumRepository configRepository;
//
////  private ConfigSourceType someSourceType;
//
//  @Before
//  public void setUp() throws Exception {
//    someNamespace = "someName";
//  }
//
//  @Test
//  public void testWhenHasContent() throws Exception {
//    Properties someProperties = new Properties();
//    String key = ConfigConsts.CONFIG_FILE_CONTENT_KEY;
//    String someValue = "someValue";
//    someProperties.setProperty(key, someValue);
//
////    someSourceType = ConfigSourceType.LOCAL;
////
////    when(configRepository.getDatum()).thenReturn(someProperties);
////    when(configRepository.getSourceType()).thenReturn(someSourceType);
////
////    JsonConfigFile configFile = new JsonConfigFile(someNamespace, configRepository);
////
////    assertEquals(ConfigFileFormat.JSON, configFile.getConfigFileFormat());
////    assertEquals(someNamespace, configFile.getNamespace());
////    assertTrue(configFile.hasContent());
////    assertEquals(someValue, configFile.getContent());
////    assertEquals(someSourceType, configFile.getSourceType());
//  }
//
//  @Test
//  public void testWhenHasNoContent() throws Exception {
//    when(configRepository.getDatum()).thenReturn(null);
//
////    JsonConfigFile configFile = new JsonConfigFile(someNamespace, configRepository);
////
////    assertFalse(configFile.hasContent());
////    assertNull(configFile.getContent());
//  }
//
//  @Test
//  public void testWhenConfigRepositoryHasError() throws Exception {
//    when(configRepository.getDatum()).thenThrow(new RuntimeException("someError"));
//
////    JsonConfigFile configFile = new JsonConfigFile(someNamespace, configRepository);
////
////    assertFalse(configFile.hasContent());
////    assertNull(configFile.getContent());
////    assertEquals(ConfigSourceType.NONE, configFile.getSourceType());
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
////    someSourceType = ConfigSourceType.LOCAL;
////
////    when(configRepository.getDatum()).thenReturn(someProperties);
////    when(configRepository.getSourceType()).thenReturn(someSourceType);
////
////    JsonConfigFile configFile = new JsonConfigFile(someNamespace, configRepository);
////
////    assertEquals(someValue, configFile.getContent());
////    assertEquals(someSourceType, configFile.getSourceType());
////
////    Properties anotherProperties = new Properties();
////    anotherProperties.setProperty(key, anotherValue);
////
////    ConfigSourceType anotherSourceType = ConfigSourceType.REMOTE;
////    when(configRepository.getSourceType()).thenReturn(anotherSourceType);
////
////    configFile.onRepositoryChange(someNamespace, anotherProperties);
////
////    assertEquals(anotherValue, configFile.getContent());
////    assertEquals(anotherSourceType, configFile.getSourceType());
//  }
//
//  @Test
//  public void testWhenConfigRepositoryHasErrorAndThenRecovered() throws Exception {
//    Properties someProperties = new Properties();
//    String key = ConfigConsts.CONFIG_FILE_CONTENT_KEY;
//    String someValue = "someValue";
//    someProperties.setProperty(key, someValue);
//
////    someSourceType = ConfigSourceType.LOCAL;
////
////    when(configRepository.getDatum()).thenThrow(new RuntimeException("someError"));
////    when(configRepository.getSourceType()).thenReturn(someSourceType);
////
////    JsonConfigFile configFile = new JsonConfigFile(someNamespace, configRepository);
////
////    assertFalse(configFile.hasContent());
////    assertNull(configFile.getContent());
////    assertEquals(ConfigSourceType.NONE, configFile.getSourceType());
////
////    configFile.onRepositoryChange(someNamespace, someProperties);
////
////    assertTrue(configFile.hasContent());
////    assertEquals(someValue, configFile.getContent());
////    assertEquals(someSourceType, configFile.getSourceType());
//  }
//}
