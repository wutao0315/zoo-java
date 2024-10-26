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
package com.wxius.framework.zoo.config.data.importer;

import com.wxius.framework.zoo.config.data.extension.initialize.ZooClientExtensionInitializeFactory;
import com.wxius.framework.zoo.config.data.injector.ZooConfigDataInjectorCustomizer;
import com.wxius.framework.zoo.config.data.internals.PureZooConfigFactory;
import com.wxius.framework.zoo.config.data.system.ZooClientSystemPropertyInitializer;
import com.wxius.framework.zoo.config.data.util.Slf4jLogMessageFormatter;
import com.wxius.framework.zoo.core.utils.DeferredLogger;
import com.wxius.framework.zoo.spi.DatumFactory;
import com.wxius.framework.zoo.spring.config.PropertySourcesConstants;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
class ZooConfigDataLoaderInitializer {

  private static volatile boolean INITIALIZED = false;

  private final DeferredLogFactory logFactory;

  private final Log log;

  private final Binder binder;

  private final BindHandler bindHandler;

  private final ConfigurableBootstrapContext bootstrapContext;

  public ZooConfigDataLoaderInitializer(DeferredLogFactory logFactory,
      Binder binder, BindHandler bindHandler,
      ConfigurableBootstrapContext bootstrapContext) {
    this.logFactory = logFactory;
    this.log = logFactory.getLog(ZooConfigDataLoaderInitializer.class);
    this.binder = binder;
    this.bindHandler = bindHandler;
    this.bootstrapContext = bootstrapContext;
  }

  /**
   * init zoo client (only once)
   *
   * @return initial sources as placeholders or empty list if already initialized
   */
  public List<PropertySource<?>> initZooClient() {
    if (INITIALIZED) {
      return Collections.emptyList();
    }
    synchronized (ZooConfigDataLoaderInitializer.class) {
      if (INITIALIZED) {
        return Collections.emptyList();
      }
      this.initZooClientInternal();
      INITIALIZED = true;
      if (this.forceDisableZooBootstrap()) {
        // force disable zoo bootstrap to avoid conflict
        Map<String, Object> map = new HashMap<>();
        map.put(PropertySourcesConstants.ZOO_BOOTSTRAP_ENABLED, "false");
        map.put(PropertySourcesConstants.ZOO_BOOTSTRAP_EAGER_LOAD_ENABLED, "false");
        // provide initial sources as placeholders to avoid duplicate loading
        return Arrays.asList(
            new ZooConfigEmptyPropertySource(
                PropertySourcesConstants.ZOO_PROPERTY_SOURCE_NAME),
            new MapPropertySource(PropertySourcesConstants.ZOO_BOOTSTRAP_PROPERTY_SOURCE_NAME,
                Collections.unmodifiableMap(map)));
      }
      // provide initial sources as placeholders to avoid duplicate loading
      return Arrays.asList(
          new ZooConfigEmptyPropertySource(PropertySourcesConstants.ZOO_PROPERTY_SOURCE_NAME),
          new ZooConfigEmptyPropertySource(
              PropertySourcesConstants.ZOO_BOOTSTRAP_PROPERTY_SOURCE_NAME));
    }
  }

  private void initZooClientInternal() {
    new ZooClientSystemPropertyInitializer(this.logFactory)
        .initializeSystemProperty(this.binder, this.bindHandler);
    new ZooClientExtensionInitializeFactory(this.logFactory,
        this.bootstrapContext).initializeExtension(this.binder, this.bindHandler);
    DeferredLogger.enable();
    ZooConfigDataInjectorCustomizer.register(DatumFactory.class,
        PureZooConfigFactory::new);
  }

  private boolean forceDisableZooBootstrap() {
    boolean bootstrapEnabled = this.binder
        .bind(this.camelCasedToKebabCase(PropertySourcesConstants.ZOO_BOOTSTRAP_ENABLED),
            Bindable.of(Boolean.class),
            this.bindHandler)
        .orElse(false);
    if (bootstrapEnabled) {
      this.log.warn(Slf4jLogMessageFormatter.format(
          "zoo bootstrap is force disabled. please don't configure the property [{}=true] and [spring.config.import=zoo://...] at the same time",
          PropertySourcesConstants.ZOO_BOOTSTRAP_ENABLED));
      return true;
    }
    boolean bootstrapEagerLoadEnabled = this.binder
        .bind(this.camelCasedToKebabCase(
            PropertySourcesConstants.ZOO_BOOTSTRAP_EAGER_LOAD_ENABLED),
            Bindable.of(Boolean.class),
            this.bindHandler)
        .orElse(false);
    if (bootstrapEagerLoadEnabled) {
      this.log.warn(Slf4jLogMessageFormatter.format(
          "zoo bootstrap eager load is force disabled. please don't configure the property [{}=true] and [spring.config.import=zoo://...] at the same time",
          PropertySourcesConstants.ZOO_BOOTSTRAP_EAGER_LOAD_ENABLED));
      return true;
    }
    return false;
  }

  /**
   * {@link ConfigurationPropertyName#isValid(java.lang.CharSequence)}
   *
   * @param source origin propertyName
   * @return valid propertyName
   */
  private String camelCasedToKebabCase(String source) {
    if (ConfigurationPropertyName.isValid(source)) {
      return source;
    }
    StringBuilder stringBuilder = new StringBuilder(source.length() * 2);
    for (char ch : source.toCharArray()) {
      if (Character.isUpperCase(ch)) {
        stringBuilder.append("-").append(Character.toLowerCase(ch));
        continue;
      }
      stringBuilder.append(ch);
    }
    return stringBuilder.toString();
  }
}
