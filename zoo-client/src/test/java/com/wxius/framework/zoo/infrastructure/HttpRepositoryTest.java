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
//import static org.junit.Assert.assertArrayEquals;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertTrue;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.any;
//import static org.mockito.Mockito.atLeast;
//import static org.mockito.Mockito.doAnswer;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.spy;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import com.wxius.framework.zoo.build.MockInjector;
////import com.wxius.framework.zoo.core.dto.ZooConfig;
////import com.wxius.framework.zoo.core.dto.ZooConfigNotification;
////import com.wxius.framework.zoo.core.dto.ZooNotificationMessages;
////import com.wxius.framework.zoo.core.dto.ServiceDTO;
//import com.wxius.framework.zoo.core.signature.Signature;
////import com.wxius.framework.zoo.enums.ConfigSourceType;
//import com.wxius.framework.zoo.exceptions.ZooException;
//import com.wxius.framework.zoo.exceptions.ZooStatusCodeException;
//import com.wxius.framework.zoo.util.ConfigUtil;
//import com.wxius.framework.zoo.util.OrderedProperties;
//import com.wxius.framework.zoo.util.factory.PropertiesFactory;
//import com.wxius.framework.zoo.util.http.HttpRequest;
//import com.wxius.framework.zoo.util.http.HttpResponse;
//import com.wxius.framework.zoo.util.http.HttpClient;
//import com.google.common.collect.ImmutableMap;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.google.common.net.HttpHeaders;
//import com.google.common.net.UrlEscapers;
//import com.google.common.util.concurrent.SettableFuture;
//import com.google.gson.Gson;
//import java.lang.reflect.Type;
//import java.util.List;
//import java.util.Map;
//import java.util.Properties;
//import java.util.concurrent.TimeUnit;
//import javax.servlet.http.HttpServletResponse;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Mock;
//import org.mockito.invocation.InvocationOnMock;
//import org.mockito.junit.MockitoJUnitRunner;
//import org.mockito.stubbing.Answer;
//
///**
// * Created by Jason on 4/9/16.
// */
//@RunWith(MockitoJUnitRunner.class)
//public class HttpRepositoryTest {
//
////  @Mock
////  private ConfigServiceLocator configServiceLocator;
//  private String someNamespace;
//  private String someServerUrl;
//  private ConfigUtil configUtil;
//  private HttpClient httpClient;
////  @Mock
////  private static HttpResponse<ZooConfig> someResponse;
////  @Mock
////  private static HttpResponse<List<ZooConfigNotification>> pollResponse;
////  private RemoteConfigLongPollService remoteConfigLongPollService;
//  @Mock
//  private PropertiesFactory propertiesFactory;
//
//  private static String someAppId;
//  private static String someCluster;
//  private static String someSecret;
//
//  @Before
//  public void setUp() throws Exception {
//    someNamespace = "someName";
//
////    when(pollResponse.getStatusCode()).thenReturn(HttpServletResponse.SC_NOT_MODIFIED);
//
//    configUtil = new MockConfigUtil();
//    MockInjector.setInstance(ConfigUtil.class, configUtil);
//
//    someServerUrl = "http://someServer";
//
////    ServiceDTO serviceDTO = mock(ServiceDTO.class);
//
////    when(serviceDTO.getHomepageUrl()).thenReturn(someServerUrl);
////    when(configServiceLocator.getConfigServices()).thenReturn(Lists.newArrayList(serviceDTO));
////    MockInjector.setInstance(ConfigServiceLocator.class, configServiceLocator);
//
//    httpClient = spy(new MockHttpClient());
//    MockInjector.setInstance(HttpClient.class, httpClient);
//
////    remoteConfigLongPollService = new RemoteConfigLongPollService();
//
////    MockInjector.setInstance(RemoteConfigLongPollService.class, remoteConfigLongPollService);
//
//    when(propertiesFactory.getPropertiesInstance()).thenAnswer(new Answer<Properties>() {
//      @Override
//      public Properties answer(InvocationOnMock invocation) {
//        return new Properties();
//      }
//    });
//    MockInjector.setInstance(PropertiesFactory.class, propertiesFactory);
//
//    someAppId = "someAppId";
//    someCluster = "someCluster";
//  }
//
//  @After
//  public void tearDown() throws Exception {
//    MockInjector.reset();
//  }
//
//  @Test
//  public void testLoadConfig() throws Exception {
//    String someKey = "someKey";
//    String someValue = "someValue";
//    Map<String, String> configurations = Maps.newHashMap();
//    configurations.put(someKey, someValue);
////    ZooConfig someZooConfig = assembleZooConfig(configurations);
////
////    when(someResponse.getStatusCode()).thenReturn(200);
////    when(someResponse.getBody()).thenReturn(someZooConfig);
//
//    HttpRepository remoteConfigRepository = new HttpRepository(someNamespace);
//
//    Properties config = remoteConfigRepository.getDatum();
//
//    assertEquals(configurations, config);
////    assertEquals(ConfigSourceType.REMOTE, remoteConfigRepository.getSourceType());
////    remoteConfigLongPollService.stopLongPollingRefresh();
//  }
//
//  @Test
//  public void testLoadConfigWithOrderedProperties() throws Exception {
//    String someKey = "someKey";
//    String someValue = "someValue";
//    Map<String, String> configurations = Maps.newLinkedHashMap();
//    configurations.put(someKey, someValue);
//    configurations.put("someKey2", "someValue2");
////    ZooConfig someZooConfig = assembleZooConfig(configurations);
////
////    when(someResponse.getStatusCode()).thenReturn(200);
////    when(someResponse.getBody()).thenReturn(someZooConfig);
//    when(propertiesFactory.getPropertiesInstance()).thenAnswer(new Answer<Properties>() {
//      @Override
//      public Properties answer(InvocationOnMock invocation) {
//        return new OrderedProperties();
//      }
//    });
//
//    HttpRepository remoteConfigRepository = new HttpRepository(someNamespace);
//
//    Properties config = remoteConfigRepository.getDatum();
//
//    assertTrue(config instanceof OrderedProperties);
//    assertEquals(configurations, config);
////    assertEquals(ConfigSourceType.REMOTE, remoteConfigRepository.getSourceType());
////    remoteConfigLongPollService.stopLongPollingRefresh();
//
//    String[] actualArrays = config.keySet().toArray(new String[]{});
//    String[] expectedArrays = {"someKey", "someKey2"};
//    assertArrayEquals(expectedArrays, actualArrays);
//  }
//
//  @Test
//  public void testLoadConfigWithAccessKeySecret() throws Exception {
//    someSecret = "someSecret";
//    String someKey = "someKey";
//    String someValue = "someValue";
//    Map<String, String> configurations = Maps.newHashMap();
//    configurations.put(someKey, someValue);
////    ZooConfig someZooConfig = assembleZooConfig(configurations);
////
////    when(someResponse.getStatusCode()).thenReturn(200);
////    when(someResponse.getBody()).thenReturn(someZooConfig);
////    doAnswer(new Answer<HttpResponse<ZooConfig>>() {
////      @Override
////      public HttpResponse<ZooConfig> answer(InvocationOnMock invocation) throws Throwable {
////        HttpRequest request = invocation.getArgument(0, HttpRequest.class);
////        Map<String, String> headers = request.getHeaders();
////        assertNotNull(headers);
////        assertTrue(headers.containsKey(Signature.HTTP_HEADER_TIMESTAMP));
////        assertTrue(headers.containsKey(HttpHeaders.AUTHORIZATION));
////
////        return someResponse;
////      }
////    }).when(httpClient).doGet(any(HttpRequest.class), any(Class.class));
//
//    HttpRepository remoteConfigRepository = new HttpRepository(someNamespace);
//
//    Properties config = remoteConfigRepository.getDatum();
//
//    assertEquals(configurations, config);
////    assertEquals(ConfigSourceType.REMOTE, remoteConfigRepository.getSourceType());
////    remoteConfigLongPollService.stopLongPollingRefresh();
//  }
//
//  @Test(expected = ZooException.class)
//  public void testGetRemoteConfigWithServerError() throws Exception {
//
////    when(someResponse.getStatusCode()).thenReturn(500);
//
//    HttpRepository remoteConfigRepository = new HttpRepository(someNamespace);
//
//    //must stop the long polling before exception occurred
////    remoteConfigLongPollService.stopLongPollingRefresh();
//
//    remoteConfigRepository.getDatum();
//  }
//
//  @Test(expected = ZooException.class)
//  public void testGetRemoteConfigWithNotFount() throws Exception {
//
////    when(someResponse.getStatusCode()).thenReturn(404);
//
//    HttpRepository remoteConfigRepository = new HttpRepository(someNamespace);
//
//    //must stop the long polling before exception occurred
////    remoteConfigLongPollService.stopLongPollingRefresh();
//
//    remoteConfigRepository.getDatum();
//  }
//
//  @Test
//  public void testRepositoryChangeListener() throws Exception {
//    Map<String, String> configurations = ImmutableMap.of("someKey", "someValue");
////    ZooConfig someZooConfig = assembleZooConfig(configurations);
////
////    when(someResponse.getStatusCode()).thenReturn(200);
////    when(someResponse.getBody()).thenReturn(someZooConfig);
//
//    RepositoryChangeListener someListener = mock(RepositoryChangeListener.class);
//    HttpRepository remoteConfigRepository = new HttpRepository(someNamespace);
//    remoteConfigRepository.addChangeListener(someListener);
//    final ArgumentCaptor<Properties> captor = ArgumentCaptor.forClass(Properties.class);
//
//    Map<String, String> newConfigurations = ImmutableMap.of("someKey", "anotherValue");
////    ZooConfig newZooConfig = assembleZooConfig(newConfigurations);
////
////    when(someResponse.getBody()).thenReturn(newZooConfig);
//
//    remoteConfigRepository.sync();
//
//    verify(someListener, times(1)).onRepositoryChange(eq(someNamespace), captor.capture());
//
//    assertEquals(newConfigurations, captor.getValue());
//
////    remoteConfigLongPollService.stopLongPollingRefresh();
//  }
//
//  @Test
//  public void testLongPollingRefresh() throws Exception {
//    Map<String, String> configurations = ImmutableMap.of("someKey", "someValue");
////    ZooConfig someZooConfig = assembleZooConfig(configurations);
////
////    when(someResponse.getStatusCode()).thenReturn(200);
////    when(someResponse.getBody()).thenReturn(someZooConfig);
//
//    final SettableFuture<Boolean> longPollFinished = SettableFuture.create();
//    RepositoryChangeListener someListener = mock(RepositoryChangeListener.class);
//    doAnswer(new Answer<Void>() {
//
//      @Override
//      public Void answer(InvocationOnMock invocation) throws Throwable {
//        longPollFinished.set(true);
//        return null;
//      }
//
//    }).when(someListener).onRepositoryChange(any(String.class), any(Properties.class));
//
//    HttpRepository remoteConfigRepository = new HttpRepository(someNamespace);
//    remoteConfigRepository.addChangeListener(someListener);
//    final ArgumentCaptor<Properties> captor = ArgumentCaptor.forClass(Properties.class);
//
//    Map<String, String> newConfigurations = ImmutableMap.of("someKey", "anotherValue");
////    ZooConfig newZooConfig = assembleZooConfig(newConfigurations);
////
////    ZooNotificationMessages notificationMessages = new ZooNotificationMessages();
////    String someKey = "someKey";
////    long someNotificationId = 1;
////    notificationMessages.put(someKey, someNotificationId);
////
////    ZooConfigNotification someNotification = mock(ZooConfigNotification.class);
////    when(someNotification.getNamespaceName()).thenReturn(someNamespace);
////    when(someNotification.getMessages()).thenReturn(notificationMessages);
////
////    when(pollResponse.getStatusCode()).thenReturn(HttpServletResponse.SC_OK);
////    when(pollResponse.getBody()).thenReturn(Lists.newArrayList(someNotification));
////    when(someResponse.getBody()).thenReturn(newZooConfig);
//
//    longPollFinished.get(30_000, TimeUnit.MILLISECONDS);
//
////    remoteConfigLongPollService.stopLongPollingRefresh();
//
//    verify(someListener, times(1)).onRepositoryChange(eq(someNamespace), captor.capture());
//    assertEquals(newConfigurations, captor.getValue());
//
////    final ArgumentCaptor<HttpRequest> httpRequestArgumentCaptor = ArgumentCaptor
////        .forClass(HttpRequest.class);
////    verify(httpClient, atLeast(2)).doGet(httpRequestArgumentCaptor.capture(), eq(ZooConfig.class));
//
////    HttpRequest request = httpRequestArgumentCaptor.getValue();
//
////    assertTrue(request.getUrl().contains("messages=%7B%22details%22%3A%7B%22someKey%22%3A1%7D%7D"));
//  }
//
//  @Test
//  public void testAssembleQueryConfigUrl() throws Exception {
//    Gson gson = new Gson();
//    String someUri = "http://someServer";
//    String someAppId = "someAppId";
//    String someCluster = "someCluster+ &.-_someSign";
//    String someReleaseKey = "20160705193346-583078ef5716c055+20160705193308-31c471ddf9087c3f";
//
////    ZooNotificationMessages notificationMessages = new ZooNotificationMessages();
////    String someKey = "someKey";
////    long someNotificationId = 1;
////    String anotherKey = "anotherKey";
////    long anotherNotificationId = 2;
////    notificationMessages.put(someKey, someNotificationId);
////    notificationMessages.put(anotherKey, anotherNotificationId);
////
////    HttpRepository remoteConfigRepository = new HttpRepository(someNamespace);
////    ZooConfig someZooConfig = mock(ZooConfig.class);
////    when(someZooConfig.getReleaseKey()).thenReturn(someReleaseKey);
//
////    String queryConfigUrl = remoteConfigRepository
////        .assembleQueryDatumUrl(someUri, someAppId, someCluster, someNamespace, null,
////            notificationMessages,
////            someZooConfig);
//
////    remoteConfigLongPollService.stopLongPollingRefresh();
////    assertTrue(queryConfigUrl
////        .contains(
////            "http://someServer/configs/someAppId/someCluster+%20&.-_someSign/" + someNamespace));
////    assertTrue(queryConfigUrl
////        .contains("releaseKey=20160705193346-583078ef5716c055%2B20160705193308-31c471ddf9087c3f"));
////    assertTrue(queryConfigUrl
////        .contains("messages=" + UrlEscapers.urlFormParameterEscaper()
////            .escape(gson.toJson(notificationMessages))));
//  }
//
////  private ZooConfig assembleZooConfig(Map<String, String> configurations) {
////    String someAppId = "appId";
////    String someClusterName = "cluster";
////    String someReleaseKey = "1";
////    ZooConfig zooConfig =
////        new ZooConfig(someAppId, someClusterName, someNamespace, someReleaseKey);
////
////    zooConfig.setConfigurations(configurations);
////
////    return zooConfig;
////  }
//
//  public static class MockConfigUtil extends ConfigUtil {
//
//    @Override
//    public String getAppId() {
//      return someAppId;
//    }
//
//    @Override
//    public String getCluster() {
//      return someCluster;
//    }
//
//    @Override
//    public String getAccessKeySecret() {
//      return someSecret;
//    }
//
//    @Override
//    public String getDataCenter() {
//      return null;
//    }
//
//    @Override
//    public int getLoadConfigQPS() {
//      return 200;
//    }
//
//    @Override
//    public int getLongPollQPS() {
//      return 200;
//    }
//
//    @Override
//    public long getOnErrorRetryInterval() {
//      return 10;
//    }
//
//    @Override
//    public TimeUnit getOnErrorRetryIntervalTimeUnit() {
//      return TimeUnit.MILLISECONDS;
//    }
//
//    @Override
//    public long getLongPollingInitialDelayInMills() {
//      return 0;
//    }
//  }
//
//  public static class MockHttpClient implements HttpClient {
//
//    @Override
//    public <T> HttpResponse<T> doGet(HttpRequest httpRequest, Class<T> responseType) {
//      if (someResponse.getStatusCode() == 200 || someResponse.getStatusCode() == 304) {
//        return (HttpResponse<T>) someResponse;
//      }
//      throw new ZooStatusCodeException(someResponse.getStatusCode(),
//              String.format("Http request failed due to status code: %d",
//          someResponse.getStatusCode()));
//    }
//
//    @Override
//    public <T> HttpResponse<T> doGet(HttpRequest httpRequest, Type responseType) {
//      try {
//        TimeUnit.MILLISECONDS.sleep(50);
//      } catch (InterruptedException e) {
//      }
//      return (HttpResponse<T>) pollResponse;
//    }
//  }
//
//}
