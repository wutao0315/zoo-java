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
package com.wxius.framework.zoo.spring.spi;

import com.wxius.framework.zoo.core.spi.Ordered;
import com.wxius.framework.zoo.spring.annotation.ZooAnnotationProcessor;
import com.wxius.framework.zoo.spring.annotation.SpringValueProcessor;
import com.wxius.framework.zoo.spring.property.AutoUpdateConfigChangeListener;
import com.wxius.framework.zoo.spring.property.SpringValueDefinitionProcessor;
import com.wxius.framework.zoo.spring.util.BeanRegistrationUtil;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

public class DefaultConfigPropertySourcesProcessorHelper implements ConfigPropertySourcesProcessorHelper {

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    Map<String, Object> propertySourcesPlaceholderPropertyValues = new HashMap<>();
    // to make sure the default PropertySourcesPlaceholderConfigurer's priority is higher than PropertyPlaceholderConfigurer
    propertySourcesPlaceholderPropertyValues.put("order", 0);

    BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, PropertySourcesPlaceholderConfigurer.class,
            propertySourcesPlaceholderPropertyValues);
    BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, AutoUpdateConfigChangeListener.class);
    BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, ZooAnnotationProcessor.class);
    BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, SpringValueProcessor.class);

    processSpringValueDefinition(registry);
  }

  /**
   * For Spring 3.x versions, the BeanDefinitionRegistryPostProcessor would not be instantiated if
   * it is added in postProcessBeanDefinitionRegistry phase, so we have to manually call the
   * postProcessBeanDefinitionRegistry method of SpringValueDefinitionProcessor here...
   */
  private void processSpringValueDefinition(BeanDefinitionRegistry registry) {
    SpringValueDefinitionProcessor springValueDefinitionProcessor = new SpringValueDefinitionProcessor();

    springValueDefinitionProcessor.postProcessBeanDefinitionRegistry(registry);
  }

  @Override
  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE;
  }
}