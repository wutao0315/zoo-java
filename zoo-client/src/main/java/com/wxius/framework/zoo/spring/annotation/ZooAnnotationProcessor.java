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
package com.wxius.framework.zoo.spring.annotation;

import com.wxius.framework.zoo.Datum;
import com.wxius.framework.zoo.DatumChangeListener;
import com.wxius.framework.zoo.ConfigService;
import com.wxius.framework.zoo.build.ZooInjector;
import com.wxius.framework.zoo.model.ConfigChangeEvent;
import com.wxius.framework.zoo.spring.property.PlaceholderHelper;
import com.wxius.framework.zoo.spring.property.SpringValue;
import com.wxius.framework.zoo.spring.property.SpringValueRegistry;
import com.wxius.framework.zoo.spring.util.SpringInjector;
import com.wxius.framework.zoo.util.ConfigUtil;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;

/**
 * Zoo Annotation Processor for Spring Application
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ZooAnnotationProcessor extends ZooProcessor implements BeanFactoryAware,
    EnvironmentAware {

  private static final Logger logger = LoggerFactory.getLogger(ZooAnnotationProcessor.class);

  private static final String NAMESPACE_DELIMITER = ",";

  private static final Splitter NAMESPACE_SPLITTER = Splitter.on(NAMESPACE_DELIMITER)
      .omitEmptyStrings().trimResults();
  private static final Gson GSON = new Gson();

  private final ConfigUtil configUtil;
  private final PlaceholderHelper placeholderHelper;
  private final SpringValueRegistry springValueRegistry;

  /**
   * resolve the expression.
   */
  private ConfigurableBeanFactory configurableBeanFactory;

  private Environment environment;

  public ZooAnnotationProcessor() {
    configUtil = ZooInjector.getInstance(ConfigUtil.class);
    placeholderHelper = SpringInjector.getInstance(PlaceholderHelper.class);
    springValueRegistry = SpringInjector.getInstance(SpringValueRegistry.class);
  }

  @Override
  protected void processField(Object bean, String beanName, Field field) {
    this.processZooConfig(bean, field);
    this.processZooJsonValue(bean, beanName, field);
  }

  @Override
  protected void processMethod(final Object bean, String beanName, final Method method) {
    this.processZooConfigChangeListener(bean, method);
    this.processZooJsonValue(bean, beanName, method);
  }

  private void processZooConfig(Object bean, Field field) {
    ZooConfig annotation = AnnotationUtils.getAnnotation(field, ZooConfig.class);
    if (annotation == null) {
      return;
    }

    Preconditions.checkArgument(Datum.class.isAssignableFrom(field.getType()),
        "Invalid type: %s for field: %s, should be Config", field.getType(), field);

    final String namespace = annotation.value();
    final String resolvedNamespace = this.environment.resolveRequiredPlaceholders(namespace);
    Datum config = ConfigService.getConfig(resolvedNamespace);

    ReflectionUtils.makeAccessible(field);
    ReflectionUtils.setField(field, bean, config);
  }

  private void processZooConfigChangeListener(final Object bean, final Method method) {
    ZooConfigChangeListener annotation = AnnotationUtils
        .findAnnotation(method, ZooConfigChangeListener.class);
    if (annotation == null) {
      return;
    }
    Class<?>[] parameterTypes = method.getParameterTypes();
    Preconditions.checkArgument(parameterTypes.length == 1,
        "Invalid number of parameters: %s for method: %s, should be 1", parameterTypes.length,
        method);
    Preconditions.checkArgument(ConfigChangeEvent.class.isAssignableFrom(parameterTypes[0]),
        "Invalid parameter type: %s for method: %s, should be ConfigChangeEvent", parameterTypes[0],
        method);

    ReflectionUtils.makeAccessible(method);
    String[] namespaces = annotation.value();
    String[] annotatedInterestedKeys = annotation.interestedKeys();
    String[] annotatedInterestedKeyPrefixes = annotation.interestedKeyPrefixes();
    DatumChangeListener configChangeListener = changeEvent -> ReflectionUtils.invokeMethod(method, bean, changeEvent);

    Set<String> interestedKeys =
        annotatedInterestedKeys.length > 0 ? Sets.newHashSet(annotatedInterestedKeys) : null;
    Set<String> interestedKeyPrefixes =
        annotatedInterestedKeyPrefixes.length > 0 ? Sets.newHashSet(annotatedInterestedKeyPrefixes)
            : null;

    Set<String> resolvedNamespaces = processResolveNamespaceValue(namespaces);

    for (String namespace : resolvedNamespaces) {
      Datum config = ConfigService.getConfig(namespace);

      if (interestedKeys == null && interestedKeyPrefixes == null) {
        config.addChangeListener(configChangeListener);
      } else {
        config.addChangeListener(configChangeListener, interestedKeys, interestedKeyPrefixes);
      }
    }
  }

  /**
   * Evaluate and resolve namespaces from env/properties.
   * Split delimited namespaces
   * @param namespaces
   * @return resolved namespaces
   */
  private Set<String> processResolveNamespaceValue(String[] namespaces) {

    Set<String> resolvedNamespaces = new HashSet<>();

    for (String namespace : namespaces) {
      final String resolvedNamespace = this.environment.resolveRequiredPlaceholders(namespace);

      if (resolvedNamespace.contains(NAMESPACE_DELIMITER)) {
        resolvedNamespaces.addAll(NAMESPACE_SPLITTER.splitToList(resolvedNamespace));
      } else {
        resolvedNamespaces.add(resolvedNamespace);
      }
    }

    return resolvedNamespaces;
  }

  private void processZooJsonValue(Object bean, String beanName, Field field) {
    ZooJsonValue zooJsonValue = AnnotationUtils.getAnnotation(field, ZooJsonValue.class);
    if (zooJsonValue == null) {
      return;
    }

    String placeholder = zooJsonValue.value();
    Object propertyValue = this.resolvePropertyValue(beanName, placeholder);
    if (propertyValue == null) {
      return;
    }

    boolean accessible = field.isAccessible();
    field.setAccessible(true);
    ReflectionUtils
        .setField(field, bean, parseJsonValue((String) propertyValue, field.getGenericType()));
    field.setAccessible(accessible);

    if (configUtil.isAutoUpdateInjectedSpringPropertiesEnabled()) {
      Set<String> keys = placeholderHelper.extractPlaceholderKeys(placeholder);
      for (String key : keys) {
        SpringValue springValue = new SpringValue(key, placeholder, bean, beanName, field, true);
        springValueRegistry.register(this.configurableBeanFactory, key, springValue);
        logger.debug("Monitoring {}", springValue);
      }
    }
  }

  private void processZooJsonValue(Object bean, String beanName, Method method) {
    ZooJsonValue zooJsonValue = AnnotationUtils.getAnnotation(method, ZooJsonValue.class);
    if (zooJsonValue == null) {
      return;
    }

    String placeHolder = zooJsonValue.value();
    Object propertyValue = this.resolvePropertyValue(beanName, placeHolder);
    if (propertyValue == null) {
      return;
    }

    Type[] types = method.getGenericParameterTypes();
    Preconditions.checkArgument(types.length == 1,
        "Ignore @ZooJsonValue setter {}.{}, expecting 1 parameter, actual {} parameters",
        bean.getClass().getName(), method.getName(), method.getParameterTypes().length);

    boolean accessible = method.isAccessible();
    method.setAccessible(true);
    ReflectionUtils.invokeMethod(method, bean, parseJsonValue((String) propertyValue, types[0]));
    method.setAccessible(accessible);

    if (configUtil.isAutoUpdateInjectedSpringPropertiesEnabled()) {
      Set<String> keys = placeholderHelper.extractPlaceholderKeys(placeHolder);
      for (String key : keys) {
        SpringValue springValue = new SpringValue(key, placeHolder, bean, beanName, method, true);
        springValueRegistry.register(this.configurableBeanFactory, key, springValue);
        logger.debug("Monitoring {}", springValue);
      }
    }
  }

  @Nullable
  private Object resolvePropertyValue(String beanName, String placeHolder) {
    Object propertyValue = placeholderHelper
        .resolvePropertyValue(this.configurableBeanFactory, beanName, placeHolder);

    // propertyValue will never be null, as @ZooJsonValue will not allow that
    if (!(propertyValue instanceof String)) {
      return null;
    }

    return propertyValue;
  }

  private Object parseJsonValue(String json, Type targetType) {
    try {
      return GSON.fromJson(json, targetType);
    } catch (Throwable ex) {
      logger.error("Parsing json '{}' to type {} failed!", json, targetType, ex);
      throw ex;
    }
  }

  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    this.configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
  }

  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }
}
