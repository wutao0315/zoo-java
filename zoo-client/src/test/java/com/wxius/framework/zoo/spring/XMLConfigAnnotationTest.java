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
//package com.wxius.framework.zoo.spring;
//
//import static org.junit.Assert.assertEquals;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.doAnswer;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//
//import com.google.common.collect.Sets;
//import java.util.Collections;
//import java.util.List;
//
//import java.util.Set;
//import org.junit.Test;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Mockito;
//import org.mockito.invocation.InvocationOnMock;
//import org.mockito.stubbing.Answer;
//import org.springframework.beans.factory.BeanCreationException;
//import org.springframework.context.support.ClassPathXmlApplicationContext;
//
//import com.wxius.framework.zoo.Datum;
//import com.wxius.framework.zoo.DatumChangeListener;
//import com.wxius.framework.zoo.core.ConfigConsts;
//import com.wxius.framework.zoo.model.ConfigChangeEvent;
//import com.wxius.framework.zoo.spring.annotation.ZooConfig;
//import com.wxius.framework.zoo.spring.annotation.ZooConfigChangeListener;
//import com.google.common.collect.Lists;
//
///**
// * @author Jason Song(song_s@ctrip.com)
// */
//public class XMLConfigAnnotationTest extends AbstractSpringIntegrationTest {
//  private static final String FX_ZOO_NAMESPACE = "FX.zoo";
//
//  @Test
//  public void testZooConfig() throws Exception {
//    Datum applicationConfig = mock(Datum.class);
//    Datum fxZooConfig = mock(Datum.class);
//
//    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, applicationConfig);
//    mockConfig(FX_ZOO_NAMESPACE, fxZooConfig);
//
//    TestZooConfigBean1 bean = getBean("spring/XmlConfigAnnotationTest1.xml", TestZooConfigBean1.class);
//
//    assertEquals(applicationConfig, bean.getConfig());
//    assertEquals(applicationConfig, bean.getAnotherConfig());
//    assertEquals(fxZooConfig, bean.getYetAnotherConfig());
//  }
//
//  @Test(expected = BeanCreationException.class)
//  public void testZooConfigWithWrongFieldType() throws Exception {
//    Datum applicationConfig = mock(Datum.class);
//
//    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, applicationConfig);
//
//    getBean("spring/XmlConfigAnnotationTest2.xml", TestZooConfigBean2.class);
//  }
//
//  @Test
//  public void testZooConfigChangeListener() throws Exception {
//    Datum applicationConfig = mock(Datum.class);
//    Datum fxZooConfig = mock(Datum.class);
//
//    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, applicationConfig);
//    mockConfig(FX_ZOO_NAMESPACE, fxZooConfig);
//
//    final List<DatumChangeListener> applicationListeners = Lists.newArrayList();
//    final List<DatumChangeListener> fxZooListeners = Lists.newArrayList();
//
//    doAnswer(new Answer() {
//      @Override
//      public Object answer(InvocationOnMock invocation) throws Throwable {
//        applicationListeners.add(invocation.getArgument(0, DatumChangeListener.class));
//
//        return Void.class;
//      }
//    }).when(applicationConfig).addChangeListener(any(DatumChangeListener.class));
//
//    doAnswer(new Answer() {
//      @Override
//      public Object answer(InvocationOnMock invocation) throws Throwable {
//        fxZooListeners.add(invocation.getArgument(0, DatumChangeListener.class));
//
//        return Void.class;
//      }
//    }).when(fxZooConfig).addChangeListener(any(DatumChangeListener.class));
//
//    ConfigChangeEvent someEvent = mock(ConfigChangeEvent.class);
//    ConfigChangeEvent anotherEvent = mock(ConfigChangeEvent.class);
//
//    TestZooConfigChangeListenerBean1 bean = getBean("spring/XmlConfigAnnotationTest3.xml",
//        TestZooConfigChangeListenerBean1.class);
//
//    //PropertySourcesProcessor add listeners to listen config changed of all namespace
//    assertEquals(4, applicationListeners.size());
//    assertEquals(1, fxZooListeners.size());
//
//    for (DatumChangeListener listener : applicationListeners) {
//      listener.onChange(someEvent);
//    }
//
//    assertEquals(someEvent, bean.getChangeEvent1());
//    assertEquals(someEvent, bean.getChangeEvent2());
//    assertEquals(someEvent, bean.getChangeEvent3());
//
//    for (DatumChangeListener listener : fxZooListeners) {
//      listener.onChange(anotherEvent);
//    }
//
//    assertEquals(someEvent, bean.getChangeEvent1());
//    assertEquals(someEvent, bean.getChangeEvent2());
//    assertEquals(anotherEvent, bean.getChangeEvent3());
//  }
//
//  @Test(expected = BeanCreationException.class)
//  public void testZooConfigChangeListenerWithWrongParamType() throws Exception {
//    Datum applicationConfig = mock(Datum.class);
//
//    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, applicationConfig);
//
//    getBean("spring/XmlConfigAnnotationTest4.xml", TestZooConfigChangeListenerBean2.class);
//  }
//
//  @Test(expected = BeanCreationException.class)
//  public void testZooConfigChangeListenerWithWrongParamCount() throws Exception {
//    Datum applicationConfig = mock(Datum.class);
//
//    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, applicationConfig);
//
//    getBean("spring/XmlConfigAnnotationTest5.xml", TestZooConfigChangeListenerBean3.class);
//  }
//
//  @Test
//  public void testZooConfigChangeListenerWithInterestedKeys() throws Exception {
//    Datum applicationConfig = mock(Datum.class);
//    Datum fxZooConfig = mock(Datum.class);
//
//    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, applicationConfig);
//    mockConfig(FX_ZOO_NAMESPACE, fxZooConfig);
//
//    TestZooConfigChangeListenerWithInterestedKeysBean bean = getBean(
//        "spring/XmlConfigAnnotationTest6.xml", TestZooConfigChangeListenerWithInterestedKeysBean.class);
//
//    final ArgumentCaptor<Set> applicationConfigInterestedKeys = ArgumentCaptor.forClass(Set.class);
//    final ArgumentCaptor<Set> fxZooConfigInterestedKeys = ArgumentCaptor.forClass(Set.class);
//
//    verify(applicationConfig, times(2))
//        .addChangeListener(any(DatumChangeListener.class), applicationConfigInterestedKeys.capture(), Mockito.nullable(Set.class));
//
//    verify(fxZooConfig, times(1))
//        .addChangeListener(any(DatumChangeListener.class), fxZooConfigInterestedKeys.capture(), Mockito.nullable(Set.class));
//
//    assertEquals(2, applicationConfigInterestedKeys.getAllValues().size());
//
//    Set<String> result = Sets.newHashSet();
//    for (Set interestedKeys : applicationConfigInterestedKeys.getAllValues()) {
//      result.addAll(interestedKeys);
//    }
//    assertEquals(Sets.newHashSet("someKey", "anotherKey"), result);
//
//    assertEquals(1, fxZooConfigInterestedKeys.getAllValues().size());
//
//    assertEquals(Collections.singletonList(Sets.newHashSet("anotherKey")), fxZooConfigInterestedKeys.getAllValues());
//  }
//
//  private <T> T getBean(String xmlLocation, Class<T> beanClass) {
//    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(xmlLocation);
//
//    return context.getBean(beanClass);
//  }
//
//  public static class TestZooConfigBean1 {
//    @ZooConfig
//    private Datum config;
//    @ZooConfig(ConfigConsts.NAMESPACE_APPLICATION)
//    private Datum anotherConfig;
//    @ZooConfig(FX_ZOO_NAMESPACE)
//    private Datum yetAnotherConfig;
//
//    public Datum getConfig() {
//      return config;
//    }
//
//    public Datum getAnotherConfig() {
//      return anotherConfig;
//    }
//
//    public Datum getYetAnotherConfig() {
//      return yetAnotherConfig;
//    }
//  }
//
//  public static class TestZooConfigBean2 {
//    @ZooConfig
//    private String config;
//  }
//
//  public static class TestZooConfigChangeListenerBean1 {
//    private ConfigChangeEvent changeEvent1;
//    private ConfigChangeEvent changeEvent2;
//    private ConfigChangeEvent changeEvent3;
//
//    @ZooConfigChangeListener
//    private void onChange1(ConfigChangeEvent changeEvent) {
//      this.changeEvent1 = changeEvent;
//    }
//
//    @ZooConfigChangeListener(ConfigConsts.NAMESPACE_APPLICATION)
//    private void onChange2(ConfigChangeEvent changeEvent) {
//      this.changeEvent2 = changeEvent;
//    }
//
//    @ZooConfigChangeListener({ConfigConsts.NAMESPACE_APPLICATION, FX_ZOO_NAMESPACE})
//    private void onChange3(ConfigChangeEvent changeEvent) {
//      this.changeEvent3 = changeEvent;
//    }
//
//    public ConfigChangeEvent getChangeEvent1() {
//      return changeEvent1;
//    }
//
//    public ConfigChangeEvent getChangeEvent2() {
//      return changeEvent2;
//    }
//
//    public ConfigChangeEvent getChangeEvent3() {
//      return changeEvent3;
//    }
//  }
//
//  public static class TestZooConfigChangeListenerBean2 {
//    @ZooConfigChangeListener
//    private void onChange(String event) {
//
//    }
//  }
//
//  public static class TestZooConfigChangeListenerBean3 {
//    @ZooConfigChangeListener
//    private void onChange(ConfigChangeEvent event, String someParam) {
//
//    }
//  }
//
//  static class TestZooConfigChangeListenerWithInterestedKeysBean {
//
//    @ZooConfigChangeListener(interestedKeys = {"someKey"})
//    private void someOnChange(ConfigChangeEvent changeEvent) {}
//
//    @ZooConfigChangeListener(value = {ConfigConsts.NAMESPACE_APPLICATION, FX_ZOO_NAMESPACE},
//        interestedKeys = {"anotherKey"})
//    private void anotherOnChange(ConfigChangeEvent changeEvent) {
//
//    }
//  }
//}
