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
package com.wxius.framework.zoo.spring;

import com.wxius.framework.zoo.Datum;
import com.wxius.framework.zoo.DatumChangeListener;
//import com.wxius.framework.zoo.ConfigFileChangeListener;
import com.wxius.framework.zoo.core.ZooClientSystemConsts;
import com.wxius.framework.zoo.core.ConfigConsts;
//import com.wxius.framework.zoo.infrastructure.SimpleConfig;
//import com.wxius.framework.zoo.infrastructure.YamlConfigFile;
import com.wxius.framework.zoo.model.ConfigChange;
import com.wxius.framework.zoo.model.ConfigChangeEvent;
import com.wxius.framework.zoo.spring.annotation.ZooConfig;
import com.wxius.framework.zoo.spring.annotation.ZooConfigChangeListener;
import com.wxius.framework.zoo.spring.annotation.EnableZooConfig;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.SettableFuture;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class JavaConfigAnnotationTest extends AbstractSpringIntegrationTest {
  private static final String FX_ZOO_NAMESPACE = "FX.zoo";
  private static final String APPLICATION_YAML_NAMESPACE = "application.yaml";

  private static <T> T getBean(Class<T> beanClass, Class<?>... annotatedClasses) {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(annotatedClasses);
    return context.getBean(beanClass);
  }

  private static <T> T getSimpleBean(Class<? extends T> clazz) {
    return getBean(clazz, clazz);
  }

  @Override
  @After
  public void tearDown() throws Exception {
    // clear the system properties
    System.clearProperty(SystemPropertyKeyConstants.SIMPLE_NAMESPACE);
    System.clearProperty(SystemPropertyKeyConstants.REDIS_NAMESPACE);
    System.clearProperty(SystemPropertyKeyConstants.FROM_SYSTEM_NAMESPACE);
    System.clearProperty(SystemPropertyKeyConstants.FROM_SYSTEM_YAML_NAMESPACE);
    System.clearProperty(SystemPropertyKeyConstants.FROM_NAMESPACE_APPLICATION_KEY);
    System.clearProperty(SystemPropertyKeyConstants.FROM_NAMESPACE_APPLICATION_KEY_YAML);
    System.clearProperty(SystemPropertyKeyConstants.DELIMITED_NAMESPACES);
    System.clearProperty(ZooClientSystemConsts.ZOO_PROPERTY_NAMES_CACHE_ENABLE);
    super.tearDown();
  }

  @Test
  public void testZooConfig() throws Exception {
    Datum applicationConfig = mock(Datum.class);
    Datum fxZooConfig = mock(Datum.class);
    String someKey = "someKey";
    String someValue = "someValue";

    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, applicationConfig);
    mockConfig(FX_ZOO_NAMESPACE, fxZooConfig);

//    prepareYamlConfigFile(APPLICATION_YAML_NAMESPACE, readYamlContentAsConfigFileProperties("case9.yml"));

    TestZooConfigBean1 bean = getBean(TestZooConfigBean1.class, AppConfig1.class);

    assertEquals(applicationConfig, bean.getConfig());
    assertEquals(applicationConfig, bean.getAnotherConfig());
    assertEquals(fxZooConfig, bean.getYetAnotherConfig());

    Datum yamlConfig = bean.getYamlConfig();
    assertEquals(someValue, yamlConfig.getProperty(someKey, null));
  }

  @Test(expected = BeanCreationException.class)
  public void testZooConfigWithWrongFieldType() throws Exception {
    Datum applicationConfig = mock(Datum.class);

    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, applicationConfig);

    getBean(TestZooConfigBean2.class, AppConfig2.class);
  }

  @Test
  public void testZooConfigWithInheritance() throws Exception {
    Datum applicationConfig = mock(Datum.class);
    Datum fxZooConfig = mock(Datum.class);

    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, applicationConfig);
    mockConfig(FX_ZOO_NAMESPACE, fxZooConfig);
//    prepareYamlConfigFile(APPLICATION_YAML_NAMESPACE, readYamlContentAsConfigFileProperties("case9.yml"));

    TestZooChildConfigBean bean = getBean(TestZooChildConfigBean.class, AppConfig6.class);

    assertEquals(applicationConfig, bean.getConfig());
    assertEquals(applicationConfig, bean.getAnotherConfig());
    assertEquals(fxZooConfig, bean.getYetAnotherConfig());
    assertEquals(applicationConfig, bean.getSomeConfig());
  }

  @Test
  public void testEnableZooConfigResolveExpressionSimple() {
    String someKey = "someKey-2020-11-14-1750";
    String someValue = UUID.randomUUID().toString();
    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, mock(Datum.class));
    Datum xxxConfig = mock(Datum.class);
    when(xxxConfig.getProperty(eq(someKey), Mockito.nullable(String.class))).thenReturn(someValue);
    mockConfig("xxx", xxxConfig);

    TestEnableZooConfigResolveExpressionWithDefaultValueConfiguration configuration =
        getSimpleBean(TestEnableZooConfigResolveExpressionWithDefaultValueConfiguration.class);

    // check
    assertEquals(someValue, configuration.getSomeKey());
    verify(xxxConfig, times(1)).getProperty(eq(someKey), Mockito.nullable(String.class));
  }

  @Test
  public void testEnableZooConfigResolveExpressionFromSystemProperty() {
    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, mock(Datum.class));
    final String someKey = "someKey-2020-11-14-1750";
    final String someValue = UUID.randomUUID().toString();

    final String resolvedNamespaceName = "yyy";
    System.setProperty(SystemPropertyKeyConstants.SIMPLE_NAMESPACE, resolvedNamespaceName);

    Datum yyyConfig = mock(Datum.class);
    when(yyyConfig.getProperty(eq(someKey), Mockito.nullable(String.class))).thenReturn(someValue);
    mockConfig(resolvedNamespaceName, yyyConfig);

    TestEnableZooConfigResolveExpressionWithDefaultValueConfiguration configuration =
        getSimpleBean(TestEnableZooConfigResolveExpressionWithDefaultValueConfiguration.class);

    // check
    assertEquals(someValue, configuration.getSomeKey());
    verify(yyyConfig, times(1)).getProperty(eq(someKey), Mockito.nullable(String.class));
  }

  @Test(expected = BeanCreationException.class)
  public void testEnableZooConfigUnresolvedValueInField() {
    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, mock(Datum.class));
    mockConfig("xxx", mock(Datum.class));
    getSimpleBean(TestEnableZooConfigResolveExpressionWithDefaultValueConfiguration.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEnableZooConfigUnresolvable() {
    getSimpleBean(TestEnableZooConfigUnresolvableConfiguration.class);
  }

  @Test
  public void testZooConfigChangeListener() throws Exception {
    Datum applicationConfig = mock(Datum.class);
    Datum fxZooConfig = mock(Datum.class);

    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, applicationConfig);
    mockConfig(FX_ZOO_NAMESPACE, fxZooConfig);

    final List<DatumChangeListener> applicationListeners = Lists.newArrayList();
    final List<DatumChangeListener> fxZooListeners = Lists.newArrayList();

    doAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        applicationListeners.add(invocation.getArgument(0, DatumChangeListener.class));

        return Void.class;
      }
    }).when(applicationConfig).addChangeListener(any(DatumChangeListener.class));

    doAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        fxZooListeners.add(invocation.getArgument(0, DatumChangeListener.class));

        return Void.class;
      }
    }).when(fxZooConfig).addChangeListener(any(DatumChangeListener.class));

    ConfigChangeEvent someEvent = mock(ConfigChangeEvent.class);
    ConfigChangeEvent anotherEvent = mock(ConfigChangeEvent.class);

    TestZooConfigChangeListenerBean1 bean = getBean(TestZooConfigChangeListenerBean1.class, AppConfig3.class);

    //PropertySourcesProcessor add listeners to listen config changed of all namespace
    assertEquals(4, applicationListeners.size());
    assertEquals(1, fxZooListeners.size());

    for (DatumChangeListener listener : applicationListeners) {
      listener.onChange(someEvent);
    }

    assertEquals(someEvent, bean.getChangeEvent1());
    assertEquals(someEvent, bean.getChangeEvent2());
    assertEquals(someEvent, bean.getChangeEvent3());

    for (DatumChangeListener listener : fxZooListeners) {
      listener.onChange(anotherEvent);
    }

    assertEquals(someEvent, bean.getChangeEvent1());
    assertEquals(someEvent, bean.getChangeEvent2());
    assertEquals(anotherEvent, bean.getChangeEvent3());
  }

  @Test(expected = BeanCreationException.class)
  public void testZooConfigChangeListenerWithWrongParamType() throws Exception {
    Datum applicationConfig = mock(Datum.class);

    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, applicationConfig);

    getBean(TestZooConfigChangeListenerBean2.class, AppConfig4.class);
  }

  @Test(expected = BeanCreationException.class)
  public void testZooConfigChangeListenerWithWrongParamCount() throws Exception {
    Datum applicationConfig = mock(Datum.class);

    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, applicationConfig);

    getBean(TestZooConfigChangeListenerBean3.class, AppConfig5.class);
  }

  @Test
  public void testZooConfigChangeListenerWithInheritance() throws Exception {
    Datum applicationConfig = mock(Datum.class);
    Datum fxZooConfig = mock(Datum.class);

    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, applicationConfig);
    mockConfig(FX_ZOO_NAMESPACE, fxZooConfig);

    final List<DatumChangeListener> applicationListeners = Lists.newArrayList();
    final List<DatumChangeListener> fxZooListeners = Lists.newArrayList();

    doAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        applicationListeners.add(invocation.getArgument(0, DatumChangeListener.class));

        return Void.class;
      }
    }).when(applicationConfig).addChangeListener(any(DatumChangeListener.class));

    doAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        fxZooListeners.add(invocation.getArgument(0, DatumChangeListener.class));

        return Void.class;
      }
    }).when(fxZooConfig).addChangeListener(any(DatumChangeListener.class));

    ConfigChangeEvent someEvent = mock(ConfigChangeEvent.class);
    ConfigChangeEvent anotherEvent = mock(ConfigChangeEvent.class);

    TestZooChildConfigChangeListener bean = getBean(TestZooChildConfigChangeListener.class, AppConfig7.class);

    //PropertySourcesProcessor add listeners to listen config changed of all namespace
    assertEquals(5, applicationListeners.size());
    assertEquals(1, fxZooListeners.size());

    for (DatumChangeListener listener : applicationListeners) {
      listener.onChange(someEvent);
    }

    assertEquals(someEvent, bean.getChangeEvent1());
    assertEquals(someEvent, bean.getChangeEvent2());
    assertEquals(someEvent, bean.getChangeEvent3());
    assertEquals(someEvent, bean.getSomeChangeEvent());

    for (DatumChangeListener listener : fxZooListeners) {
      listener.onChange(anotherEvent);
    }

    assertEquals(someEvent, bean.getChangeEvent1());
    assertEquals(someEvent, bean.getChangeEvent2());
    assertEquals(anotherEvent, bean.getChangeEvent3());
    assertEquals(someEvent, bean.getSomeChangeEvent());
  }

  @Test
  public void testZooConfigChangeListenerWithInterestedKeys() throws Exception {
    Datum applicationConfig = mock(Datum.class);
    Datum fxZooConfig = mock(Datum.class);

    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, applicationConfig);
    mockConfig(FX_ZOO_NAMESPACE, fxZooConfig);

    TestZooConfigChangeListenerWithInterestedKeysBean bean = getBean(
        TestZooConfigChangeListenerWithInterestedKeysBean.class, AppConfig8.class);

    final ArgumentCaptor<Set> applicationConfigInterestedKeys = ArgumentCaptor.forClass(Set.class);
    final ArgumentCaptor<Set> fxZooConfigInterestedKeys = ArgumentCaptor.forClass(Set.class);

    verify(applicationConfig, times(2))
        .addChangeListener(any(DatumChangeListener.class), applicationConfigInterestedKeys.capture(), Mockito.nullable(Set.class));

    verify(fxZooConfig, times(1))
        .addChangeListener(any(DatumChangeListener.class), fxZooConfigInterestedKeys.capture(), Mockito.nullable(Set.class));

    assertEquals(2, applicationConfigInterestedKeys.getAllValues().size());

    Set<String> result = Sets.newHashSet();
    for (Set interestedKeys : applicationConfigInterestedKeys.getAllValues()) {
      result.addAll(interestedKeys);
    }
    assertEquals(Sets.newHashSet("someKey", "anotherKey"), result);

    assertEquals(1, fxZooConfigInterestedKeys.getAllValues().size());

    assertEquals(Collections.singletonList(Sets.newHashSet("anotherKey")), fxZooConfigInterestedKeys.getAllValues());
  }

  @Test
  public void testZooConfigChangeListenerWithInterestedKeyPrefixes() {
    Datum applicationConfig = mock(Datum.class);

    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, applicationConfig);

    TestZooConfigChangeListenerWithInterestedKeyPrefixesBean bean = getBean(
        TestZooConfigChangeListenerWithInterestedKeyPrefixesBean.class, AppConfig10.class);

    final ArgumentCaptor<Set> interestedKeyPrefixesArgumentCaptor = ArgumentCaptor
        .forClass(Set.class);

    verify(applicationConfig, times(1))
        .addChangeListener(any(DatumChangeListener.class), Mockito.nullable(Set.class),
            interestedKeyPrefixesArgumentCaptor.capture());

    assertEquals(1, interestedKeyPrefixesArgumentCaptor.getAllValues().size());

    Set<String> result = Sets.newHashSet();
    for (Set<String> interestedKeyPrefixes : interestedKeyPrefixesArgumentCaptor.getAllValues()) {
      result.addAll(interestedKeyPrefixes);
    }
    assertEquals(Sets.newHashSet("logging.level", "number"), result);
  }

  @Test
  public void testZooConfigChangeListenerWithInterestedKeyPrefixes_fire()
      throws InterruptedException {
    // default mock, useless here
    // just for speed up test without waiting
    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, mock(Datum.class));

//    SimpleConfig simpleConfig = spy(
//        this.prepareConfig(
//            TestZooConfigChangeListenerWithInterestedKeyPrefixesBean1.SPECIAL_NAMESPACE,
//            new Properties()));
//
//    mockConfig(TestZooConfigChangeListenerWithInterestedKeyPrefixesBean1.SPECIAL_NAMESPACE,
//        simpleConfig);

    TestZooConfigChangeListenerWithInterestedKeyPrefixesBean1 bean = getBean(
        TestZooConfigChangeListenerWithInterestedKeyPrefixesBean1.class, AppConfig11.class);

//    verify(simpleConfig, atLeastOnce())
//        .addChangeListener(any(DatumChangeListener.class), Mockito.nullable(Set.class),
//            anySet());

    Properties properties = new Properties();
    properties.put("logging.level.com", "debug");
    properties.put("logging.level.root", "warn");
    properties.put("number.value", "333");

//    // publish config change
//    simpleConfig.onRepositoryChange(
//        TestZooConfigChangeListenerWithInterestedKeyPrefixesBean1.SPECIAL_NAMESPACE, properties);

    // get event from bean
    ConfigChangeEvent configChangeEvent = bean.getConfigChangeEvent();
    Set<String> interestedChangedKeys = configChangeEvent.interestedChangedKeys();
    assertEquals(Sets.newHashSet("logging.level.com", "logging.level.root", "number.value"),
        interestedChangedKeys);
  }

  @Test
  public void testZooConfigChangeListenerWithYamlFile() throws Exception {
    String someKey = "someKey";
    String someValue = "someValue";
    String anotherValue = "anotherValue";

//    YamlConfigFile configFile = prepareYamlConfigFile(APPLICATION_YAML_NAMESPACE,
//        readYamlContentAsConfigFileProperties("case9.yml"));

    TestZooConfigChangeListenerWithYamlFile bean = getBean(TestZooConfigChangeListenerWithYamlFile.class, AppConfig9.class);

    Datum yamlConfig = bean.getYamlConfig();
    SettableFuture<ConfigChangeEvent> future = bean.getConfigChangeEventFuture();

    assertEquals(someValue, yamlConfig.getProperty(someKey, null));
    assertFalse(future.isDone());

//    configFile.onRepositoryChange(APPLICATION_YAML_NAMESPACE, readYamlContentAsConfigFileProperties("case9-new.yml"));

    ConfigChangeEvent configChangeEvent = future.get(100, TimeUnit.MILLISECONDS);
    ConfigChange change = configChangeEvent.getChange(someKey);
    assertEquals(someValue, change.getOldValue());
    assertEquals(anotherValue, change.getNewValue());

    assertEquals(anotherValue, yamlConfig.getProperty(someKey, null));
  }

  @Test
  public void testZooConfigChangeListenerResolveExpressionSimple() {
    // for ignore, no listener use it
    Datum ignoreConfig = mock(Datum.class);
    mockConfig("ignore.for.listener", ignoreConfig);

    Datum applicationConfig = mock(Datum.class);
    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, applicationConfig);

    System.setProperty(ZooClientSystemConsts.ZOO_PROPERTY_NAMES_CACHE_ENABLE, "true");

    getSimpleBean(TestZooConfigChangeListenerResolveExpressionSimpleConfiguration.class);

    // no using
    verify(ignoreConfig, never()).addChangeListener(any(DatumChangeListener.class));

    // one invocation for spring value auto update
    // one invocation for the @ZooConfigChangeListener annotation
    // one invocation for CachedCompositePropertySource clear cache listener
    verify(applicationConfig, times(3)).addChangeListener(any(DatumChangeListener.class));
  }


  /**
   * resolve namespace's from comma separated namespaces
   */
  @Test
  public void testZooConfigChangeListenerWithCommaSeparatedNameSpaces() {

    final String propValue = "app1,app2,app3";
    System.setProperty(SystemPropertyKeyConstants.DELIMITED_NAMESPACES, propValue);

    Datum app1Config = mock(Datum.class);
    mockConfig("app1", app1Config);

    Datum app2Config = mock(Datum.class);
    mockConfig("app2", app2Config);

    Datum app3Config = mock(Datum.class);
    mockConfig("app3", app3Config);

    getSimpleBean(TestZooConfigChangeListenerWithCommaSeparatedNameSpaces.class);

    verify(app1Config, times(1)).addChangeListener(any(DatumChangeListener.class));
    verify(app2Config, times(1)).addChangeListener(any(DatumChangeListener.class));
    verify(app3Config, times(1)).addChangeListener(any(DatumChangeListener.class));
  }

  /**
   * resolve namespace's from comma separated namespaces
   */
  @Test
  public void testZooConfigChangeListenerWithCommaSeparatedNameSpacesMergedWithOnesInValue() {

    final String propValue = "app1,app2";
    System.setProperty(SystemPropertyKeyConstants.DELIMITED_NAMESPACES, propValue);

    Datum app1Config = mock(Datum.class);
    mockConfig("app1", app1Config);

    Datum app2Config = mock(Datum.class);
    mockConfig("app2", app2Config);

    Datum appConfig = mock(Datum.class);
    mockConfig("app", appConfig);

    getSimpleBean(TestZooConfigChangeListenerWithCommaSeparatedNameSpacesMergedWithOnesInValue.class);

    verify(app1Config, times(1)).addChangeListener(any(DatumChangeListener.class));
    verify(app2Config, times(1)).addChangeListener(any(DatumChangeListener.class));
    verify(appConfig, times(1)).addChangeListener(any(DatumChangeListener.class));
  }

  /**
   * resolve namespace's name from system property.
   */
  @Test
  public void testZooConfigChangeListenerResolveExpressionFromSystemProperty() {
    Datum applicationConfig = mock(Datum.class);
    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, applicationConfig);

    final String namespaceName = "magicRedis";
    System.setProperty(SystemPropertyKeyConstants.REDIS_NAMESPACE, namespaceName);
    Datum redisConfig = mock(Datum.class);
    mockConfig(namespaceName, redisConfig);
    getSimpleBean(
        TestZooConfigChangeListenerResolveExpressionFromSystemPropertyConfiguration.class);

    // if config was used, it must be invoked on method addChangeListener 1 time
    verify(redisConfig, times(1)).addChangeListener(any(DatumChangeListener.class));
  }

  /**
   * resolve namespace from config. ${mysql.namespace} will be resolved by config from namespace
   * application.
   */
  @Test
  public void testZooConfigChangeListenerResolveExpressionFromApplicationNamespace() {
    final String namespaceKey = "mysql.namespace";
    final String namespaceName = "magicMysqlNamespaceApplication";

    Properties properties = new Properties();
    properties.setProperty(namespaceKey, namespaceName);
//    this.prepareConfig(ConfigConsts.NAMESPACE_APPLICATION, properties);

    Datum mysqlConfig = mock(Datum.class);
    mockConfig(namespaceName, mysqlConfig);

    getSimpleBean(
        TestZooConfigChangeListenerResolveExpressionFromApplicationNamespaceConfiguration.class);

    // if config was used, it must be invoked on method addChangeListener 1 time
    verify(mysqlConfig, times(1)).addChangeListener(any(DatumChangeListener.class));
  }

  @Test(expected = BeanCreationException.class)
  public void testZooConfigChangeListenerUnresolvedPlaceholder() {
    Datum applicationConfig = mock(Datum.class);
    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, applicationConfig);
    getSimpleBean(TestZooConfigChangeListenerUnresolvedPlaceholderConfiguration.class);
  }

  @Test
  public void testZooConfigChangeListenerResolveExpressionFromSelfYaml() throws IOException {
    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, mock(Datum.class));

    final String resolvedValue = "resolve.from.self.yml";
//    YamlConfigFile yamlConfigFile = prepareYamlConfigFile(resolvedValue, readYamlContentAsConfigFileProperties(resolvedValue));
    getSimpleBean(TestZooConfigChangeListenerResolveExpressionFromSelfYamlConfiguration.class);
//    verify(yamlConfigFile, times(1)).addChangeListener(any(ConfigFileChangeListener.class));
  }

  @Test
  public void testZooConfigResolveExpressionDefault() {
    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, mock(Datum.class));
    Datum defaultConfig = mock(Datum.class);
    Datum yamlConfig = mock(Datum.class);
    mockConfig("default-2020-11-14-1733", defaultConfig);
    mockConfig(APPLICATION_YAML_NAMESPACE, yamlConfig);
    TestZooConfigResolveExpressionDefaultConfiguration configuration = getSimpleBean(
        TestZooConfigResolveExpressionDefaultConfiguration.class);
    assertSame(defaultConfig, configuration.getDefaultConfig());
    assertSame(yamlConfig, configuration.getYamlConfig());
  }

  @Test
  public void testZooConfigResolveExpressionFromSystemProperty() {
    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, mock(Datum.class));
    final String namespaceName = "xxx6";
    final String yamlNamespaceName = "yyy8.yml";

    System.setProperty(SystemPropertyKeyConstants.FROM_SYSTEM_NAMESPACE, namespaceName);
    System.setProperty(SystemPropertyKeyConstants.FROM_SYSTEM_YAML_NAMESPACE, yamlNamespaceName);
    Datum config = mock(Datum.class);
    Datum yamlConfig = mock(Datum.class);
    mockConfig(namespaceName, config);
    mockConfig(yamlNamespaceName, yamlConfig);
    TestZooConfigResolveExpressionFromSystemPropertyConfiguration configuration = getSimpleBean(
        TestZooConfigResolveExpressionFromSystemPropertyConfiguration.class);
    assertSame(config, configuration.getConfig());
    assertSame(yamlConfig, configuration.getYamlConfig());
  }

  @Test(expected = BeanCreationException.class)
  public void testZooConfigUnresolvedExpression() {
    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, mock(Datum.class));
    getSimpleBean(TestZooConfigUnresolvedExpressionConfiguration.class);
  }

  @Test
  public void testZooConfigResolveExpressionFromZooConfigNamespaceApplication() {

    final String namespaceName = "xxx6";
    final String yamlNamespaceName = "yyy8.yml";
    {
      // hide variable scope
      Properties properties = new Properties();
      properties.setProperty(SystemPropertyKeyConstants.FROM_NAMESPACE_APPLICATION_KEY, namespaceName);
      properties.setProperty(SystemPropertyKeyConstants.FROM_NAMESPACE_APPLICATION_KEY_YAML, yamlNamespaceName);
//      this.prepareConfig(ConfigConsts.NAMESPACE_APPLICATION, properties);
    }
    final Datum config = mock(Datum.class);
    final Datum yamlConfig = mock(Datum.class);
    mockConfig(namespaceName, config);
    mockConfig(yamlNamespaceName, yamlConfig);
    TestZooConfigResolveExpressionFromZooConfigNamespaceApplication configuration = getSimpleBean(
        TestZooConfigResolveExpressionFromZooConfigNamespaceApplication.class);
    assertSame(config, configuration.getConfig());
    assertSame(yamlConfig, configuration.getYamlConfig());
  }

  private static class SystemPropertyKeyConstants {

    static final String SIMPLE_NAMESPACE = "simple.namespace";
    static final String REDIS_NAMESPACE = "redis.namespace";
    static final String FROM_SYSTEM_NAMESPACE = "from.system.namespace";
    static final String FROM_SYSTEM_YAML_NAMESPACE = "from.system.yaml.namespace";
    static final String FROM_NAMESPACE_APPLICATION_KEY = "from.namespace.application.key";
    static final String FROM_NAMESPACE_APPLICATION_KEY_YAML = "from.namespace.application.key.yaml";
    static final String DELIMITED_NAMESPACES = "delimited.namespaces";
  }

  @EnableZooConfig
  protected static class TestZooConfigResolveExpressionDefaultConfiguration {

    @ZooConfig(value = "${simple.namespace:default-2020-11-14-1733}")
    private Datum defaultConfig;

    @ZooConfig(value = "${simple.yaml.namespace:" + APPLICATION_YAML_NAMESPACE + "}")
    private Datum yamlConfig;

    public Datum getDefaultConfig() {
      return defaultConfig;
    }

    public Datum getYamlConfig() {
      return yamlConfig;
    }
  }

  @EnableZooConfig
  protected static class TestZooConfigResolveExpressionFromSystemPropertyConfiguration {

    @ZooConfig(value = "${from.system.namespace}")
    private Datum config;

    @ZooConfig(value = "${from.system.yaml.namespace}")
    private Datum yamlConfig;

    public Datum getConfig() {
      return config;
    }

    public Datum getYamlConfig() {
      return yamlConfig;
    }
  }

  @EnableZooConfig
  protected static class TestZooConfigUnresolvedExpressionConfiguration {

    @ZooConfig(value = "${so.complex.to.resolve}")
    private Datum config;
  }

  @EnableZooConfig
  protected static class TestZooConfigResolveExpressionFromZooConfigNamespaceApplication {

    @ZooConfig(value = "${from.namespace.application.key}")
    private Datum config;

    @ZooConfig(value = "${from.namespace.application.key.yaml}")
    private Datum yamlConfig;

    public Datum getConfig() {
      return config;
    }

    public Datum getYamlConfig() {
      return yamlConfig;
    }
  }


  @Configuration
  @EnableZooConfig
  static class TestZooConfigChangeListenerResolveExpressionSimpleConfiguration {

    @ZooConfigChangeListener("${simple.application:application}")
    private void onChange(ConfigChangeEvent event) {
    }
  }

  @Configuration
  @EnableZooConfig
  static class TestZooConfigChangeListenerWithCommaSeparatedNameSpaces {

    @ZooConfigChangeListener("${" + SystemPropertyKeyConstants.DELIMITED_NAMESPACES + "}")
    private void onChange(ConfigChangeEvent changeEvent) {
    }
  }

  @Configuration
  @EnableZooConfig
  static class TestZooConfigChangeListenerWithCommaSeparatedNameSpacesMergedWithOnesInValue {

    @ZooConfigChangeListener(value = {"app", "${" + SystemPropertyKeyConstants.DELIMITED_NAMESPACES + "}"})
    private void onChange(ConfigChangeEvent changeEvent) {
    }
  }

  @Configuration
  @EnableZooConfig
  static class TestZooConfigChangeListenerResolveExpressionFromSystemPropertyConfiguration {

    @ZooConfigChangeListener("${redis.namespace}")
    private void onChange(ConfigChangeEvent event) {
    }
  }

  @Configuration
  @EnableZooConfig
  static class TestZooConfigChangeListenerResolveExpressionFromApplicationNamespaceConfiguration {

    @ZooConfigChangeListener(value = {ConfigConsts.NAMESPACE_APPLICATION,
        "${mysql.namespace}"})
    private void onChange(ConfigChangeEvent event) {
    }
  }

  @Configuration
  @EnableZooConfig
  static class TestZooConfigChangeListenerUnresolvedPlaceholderConfiguration {
    @ZooConfigChangeListener(value = {ConfigConsts.NAMESPACE_APPLICATION,
        "${i.can.not.be.resolved}"})
    private void onChange(ConfigChangeEvent event) {
    }
  }

  @Configuration
  @EnableZooConfig("resolve.from.self.yml")
  static class TestZooConfigChangeListenerResolveExpressionFromSelfYamlConfiguration {

    /**
     * value in file src/test/resources/spring/yaml/resolve.from.self.yml
     */
    @ZooConfigChangeListener("${i.can.resolve.from.self}")
    private void onChange(ConfigChangeEvent event) {
    }
  }

  @Configuration
  @EnableZooConfig(value = {ConfigConsts.NAMESPACE_APPLICATION, "${simple.namespace:xxx}"})
  static class TestEnableZooConfigResolveExpressionWithDefaultValueConfiguration {

    @Value("${someKey-2020-11-14-1750}")
    private String someKey;

    public String getSomeKey() {
      return this.someKey;
    }
  }

  @Configuration
  @EnableZooConfig(value = "${unresolvable.property}")
  static class TestEnableZooConfigUnresolvableConfiguration {

  }

  @Configuration
  @EnableZooConfig
  static class AppConfig1 {
    @Bean
    public TestZooConfigBean1 bean() {
      return new TestZooConfigBean1();
    }
  }

  @Configuration
  @EnableZooConfig
  static class AppConfig2 {
    @Bean
    public TestZooConfigBean2 bean() {
      return new TestZooConfigBean2();
    }
  }

  @Configuration
  @EnableZooConfig
  static class AppConfig3 {
    @Bean
    public TestZooConfigChangeListenerBean1 bean() {
      return new TestZooConfigChangeListenerBean1();
    }
  }

  @Configuration
  @EnableZooConfig
  static class AppConfig4 {
    @Bean
    public TestZooConfigChangeListenerBean2 bean() {
      return new TestZooConfigChangeListenerBean2();
    }
  }

  @Configuration
  @EnableZooConfig
  static class AppConfig5 {
    @Bean
    public TestZooConfigChangeListenerBean3 bean() {
      return new TestZooConfigChangeListenerBean3();
    }
  }

  @Configuration
  @EnableZooConfig
  static class AppConfig6 {
    @Bean
    public TestZooChildConfigBean bean() {
      return new TestZooChildConfigBean();
    }
  }

  @Configuration
  @EnableZooConfig
  static class AppConfig7 {
    @Bean
    public TestZooChildConfigChangeListener bean() {
      return new TestZooChildConfigChangeListener();
    }
  }

  @Configuration
  @EnableZooConfig
  static class AppConfig8 {
    @Bean
    public TestZooConfigChangeListenerWithInterestedKeysBean bean() {
      return new TestZooConfigChangeListenerWithInterestedKeysBean();
    }
  }

  @Configuration
  @EnableZooConfig(APPLICATION_YAML_NAMESPACE)
  static class AppConfig9 {
    @Bean
    public TestZooConfigChangeListenerWithYamlFile bean() {
      return new TestZooConfigChangeListenerWithYamlFile();
    }
  }

  @Configuration
  @EnableZooConfig
  static class AppConfig10 {
    @Bean
    public TestZooConfigChangeListenerWithInterestedKeyPrefixesBean bean() {
      return new TestZooConfigChangeListenerWithInterestedKeyPrefixesBean();
    }
  }

  @Configuration
  @EnableZooConfig
  static class AppConfig11 {
    @Bean
    public TestZooConfigChangeListenerWithInterestedKeyPrefixesBean1 bean() {
      return spy(new TestZooConfigChangeListenerWithInterestedKeyPrefixesBean1());
    }
  }

  static class TestZooConfigBean1 {
    @ZooConfig
    private Datum config;
    @ZooConfig(ConfigConsts.NAMESPACE_APPLICATION)
    private Datum anotherConfig;
    @ZooConfig(FX_ZOO_NAMESPACE)
    private Datum yetAnotherConfig;
    @ZooConfig(APPLICATION_YAML_NAMESPACE)
    private Datum yamlConfig;

    public Datum getConfig() {
      return config;
    }

    public Datum getAnotherConfig() {
      return anotherConfig;
    }

    public Datum getYetAnotherConfig() {
      return yetAnotherConfig;
    }

    public Datum getYamlConfig() {
      return yamlConfig;
    }
  }

  static class TestZooConfigBean2 {
    @ZooConfig
    private String config;
  }

  static class TestZooChildConfigBean extends TestZooConfigBean1 {

    @ZooConfig
    private Datum someConfig;

    public Datum getSomeConfig() {
      return someConfig;
    }
  }

  static class TestZooConfigChangeListenerBean1 {
    private ConfigChangeEvent changeEvent1;
    private ConfigChangeEvent changeEvent2;
    private ConfigChangeEvent changeEvent3;

    @ZooConfigChangeListener
    private void onChange1(ConfigChangeEvent changeEvent) {
      this.changeEvent1 = changeEvent;
    }

    @ZooConfigChangeListener(ConfigConsts.NAMESPACE_APPLICATION)
    private void onChange2(ConfigChangeEvent changeEvent) {
      this.changeEvent2 = changeEvent;
    }

    @ZooConfigChangeListener({ConfigConsts.NAMESPACE_APPLICATION, FX_ZOO_NAMESPACE})
    private void onChange3(ConfigChangeEvent changeEvent) {
      this.changeEvent3 = changeEvent;
    }

    public ConfigChangeEvent getChangeEvent1() {
      return changeEvent1;
    }

    public ConfigChangeEvent getChangeEvent2() {
      return changeEvent2;
    }

    public ConfigChangeEvent getChangeEvent3() {
      return changeEvent3;
    }
  }

  static class TestZooConfigChangeListenerBean2 {
    @ZooConfigChangeListener
    private void onChange(String event) {

    }
  }

  static class TestZooConfigChangeListenerBean3 {
    @ZooConfigChangeListener
    private void onChange(ConfigChangeEvent event, String someParam) {

    }
  }

  static class TestZooChildConfigChangeListener extends TestZooConfigChangeListenerBean1 {

    private ConfigChangeEvent someChangeEvent;

    @ZooConfigChangeListener
    private void someOnChange(ConfigChangeEvent changeEvent) {
      this.someChangeEvent = changeEvent;
    }

    public ConfigChangeEvent getSomeChangeEvent() {
      return someChangeEvent;
    }
  }

  static class TestZooConfigChangeListenerWithInterestedKeysBean {

    @ZooConfigChangeListener(interestedKeys = {"someKey"})
    private void someOnChange(ConfigChangeEvent changeEvent) {}

    @ZooConfigChangeListener(value = {ConfigConsts.NAMESPACE_APPLICATION, FX_ZOO_NAMESPACE},
        interestedKeys = {"anotherKey"})
    private void anotherOnChange(ConfigChangeEvent changeEvent) {

    }
  }

  private static class TestZooConfigChangeListenerWithInterestedKeyPrefixesBean {

    @ZooConfigChangeListener(interestedKeyPrefixes = {"number", "logging.level"})
    private void onChange(ConfigChangeEvent changeEvent) {
    }
  }

  private static class TestZooConfigChangeListenerWithInterestedKeyPrefixesBean1 {

    static final String SPECIAL_NAMESPACE = "special-namespace-2021";

    private final BlockingQueue<ConfigChangeEvent> configChangeEventQueue = new ArrayBlockingQueue<>(100);

    @ZooConfigChangeListener(value = SPECIAL_NAMESPACE, interestedKeyPrefixes = {"number",
        "logging.level"})
    private void onChange(ConfigChangeEvent changeEvent) {
      this.configChangeEventQueue.add(changeEvent);
    }

    public ConfigChangeEvent getConfigChangeEvent() throws InterruptedException {
      return this.configChangeEventQueue.poll(5, TimeUnit.SECONDS);
    }
  }

  static class TestZooConfigChangeListenerWithYamlFile {

    private SettableFuture<ConfigChangeEvent> configChangeEventFuture = SettableFuture.create();

    @ZooConfig(APPLICATION_YAML_NAMESPACE)
    private Datum yamlConfig;

    @ZooConfigChangeListener(APPLICATION_YAML_NAMESPACE)
    private void onChange(ConfigChangeEvent event) {
      configChangeEventFuture.set(event);
    }

    public SettableFuture<ConfigChangeEvent> getConfigChangeEventFuture() {
      return configChangeEventFuture;
    }

    public Datum getYamlConfig() {
      return yamlConfig;
    }
  }
}
