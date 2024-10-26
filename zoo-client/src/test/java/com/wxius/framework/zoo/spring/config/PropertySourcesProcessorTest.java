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
package com.wxius.framework.zoo.spring.config;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.wxius.framework.zoo.Datum;
import com.wxius.framework.zoo.DatumChangeListener;
import com.wxius.framework.zoo.build.MockInjector;
import com.wxius.framework.zoo.core.ZooClientSystemConsts;
import com.wxius.framework.zoo.core.ConfigConsts;
import com.wxius.framework.zoo.model.ConfigChangeEvent;
import com.wxius.framework.zoo.spring.AbstractSpringIntegrationTest;
import com.wxius.framework.zoo.spring.events.ZooConfigChangeEvent;
import com.wxius.framework.zoo.util.ConfigUtil;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.*;

import java.util.Properties;

public class PropertySourcesProcessorTest extends AbstractSpringIntegrationTest {

  private ConfigurableEnvironment environment;
  private ConfigurableListableBeanFactory beanFactory;
  private PropertySourcesProcessor processor;
  private MutablePropertySources propertySources;
  private ApplicationEventPublisher applicationEventPublisher;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    propertySources = mock(MutablePropertySources.class);
    environment = mock(ConfigurableEnvironment.class);
    when(environment.getPropertySources()).thenReturn(propertySources);
    beanFactory = mock(ConfigurableListableBeanFactory.class);
    applicationEventPublisher = mock(ApplicationEventPublisher.class);
    processor = new PropertySourcesProcessor();
    processor.setEnvironment(environment);
    processor.setApplicationEventPublisher(applicationEventPublisher);
  }

  @Override
  @After
  public void tearDown() throws Exception {
    super.tearDown();
    PropertySourcesProcessor.reset();
  }

  @Test
  public void testInitializePropertySources() {
    String namespaceName = "someNamespace";
    String anotherNamespaceName = "anotherNamespace";
    Datum config = mock(Datum.class);
    Datum anotherConfig = mock(Datum.class);
    mockConfig(namespaceName, config);
    mockConfig(anotherNamespaceName, anotherConfig);
    PropertySourcesProcessor.addNamespaces(Lists.newArrayList(namespaceName, anotherNamespaceName),
        0);

    processor.postProcessBeanFactory(beanFactory);

    ArgumentCaptor<CompositePropertySource> argumentCaptor = ArgumentCaptor.forClass(
        CompositePropertySource.class);
    verify(propertySources).addFirst(argumentCaptor.capture());

    CompositePropertySource compositePropertySource = argumentCaptor.getValue();
    assertEquals(2, compositePropertySource.getPropertySources().size());

    ConfigPropertySource propertySource = (ConfigPropertySource) Lists.newArrayList(
        compositePropertySource.getPropertySources()).get(0);
    ConfigPropertySource anotherPropertySource = (ConfigPropertySource) Lists.newArrayList(
        compositePropertySource.getPropertySources()).get(1);

    assertEquals(namespaceName, propertySource.getName());
    assertSame(config, propertySource.getSource());
    assertEquals(anotherNamespaceName, anotherPropertySource.getName());
    assertSame(anotherConfig, anotherPropertySource.getSource());
  }

  @Test
  public void testApplicationEvent() {
    String namespaceName = "someNamespace";
    Datum config = mock(Datum.class);
    mockConfig(namespaceName, config);
    PropertySourcesProcessor.addNamespaces(Lists.newArrayList(namespaceName), 0);
    ConfigChangeEvent someConfigChangeEvent = mock(ConfigChangeEvent.class);

    processor.postProcessBeanFactory(beanFactory);

    ArgumentCaptor<DatumChangeListener> argumentCaptor = ArgumentCaptor.forClass(
        DatumChangeListener.class);
    verify(config).addChangeListener(argumentCaptor.capture());

    DatumChangeListener listener = argumentCaptor.getValue();
    listener.onChange(someConfigChangeEvent);

    ArgumentCaptor<ZooConfigChangeEvent> eventCaptor = ArgumentCaptor.forClass(
        ZooConfigChangeEvent.class);
    verify(applicationEventPublisher).publishEvent(eventCaptor.capture());

    ZooConfigChangeEvent event = eventCaptor.getValue();
    assertSame(someConfigChangeEvent, event.getConfigChangeEvent());
  }


  @Test
  public void testOverrideSystemProperties() {
    Properties properties = new Properties();
    properties.setProperty("server.port", "8080");
    properties.setProperty(ZooClientSystemConsts.ZOO_OVERRIDE_SYSTEM_PROPERTIES, "false");
    ConfigurableEnvironment environment = mock(ConfigurableEnvironment.class);

    MutablePropertySources propertySources = new MutablePropertySources();
    propertySources.addLast(new PropertiesPropertySource(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, properties));

    when(environment.getPropertySources()).thenReturn(propertySources);
    when(environment.getProperty(PropertySourcesConstants.ZOO_BOOTSTRAP_NAMESPACES,
            ConfigConsts.NAMESPACE_APPLICATION)).thenReturn("");

    ConfigUtil configUtil = new ConfigUtil();
    configUtil = spy(configUtil);
    when(configUtil.isOverrideSystemProperties()).thenReturn(false);
    MockInjector.setInstance(ConfigUtil.class, configUtil);

    processor.setEnvironment(environment);
    processor.postProcessBeanFactory(beanFactory);

    assertTrue(propertySources.contains(PropertySourcesConstants.ZOO_PROPERTY_SOURCE_NAME));
    assertEquals(propertySources.iterator().next().getName(), StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
  }
}
