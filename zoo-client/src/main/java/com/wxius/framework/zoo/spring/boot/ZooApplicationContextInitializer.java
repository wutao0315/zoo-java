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
package com.wxius.framework.zoo.spring.boot;

import com.wxius.framework.zoo.Datum;
import com.wxius.framework.zoo.ConfigService;
import com.wxius.framework.zoo.build.ZooInjector;
import com.wxius.framework.zoo.core.ZooClientSystemConsts;
import com.wxius.framework.zoo.core.ConfigConsts;
import com.wxius.framework.zoo.core.utils.DeferredLogger;
import com.wxius.framework.zoo.spring.config.CachedCompositePropertySource;
import com.wxius.framework.zoo.spring.config.ConfigPropertySourceFactory;
import com.wxius.framework.zoo.spring.config.PropertySourcesConstants;
import com.wxius.framework.zoo.spring.util.PropertySourcesUtil;
import com.wxius.framework.zoo.spring.util.SpringInjector;
import com.wxius.framework.zoo.util.ConfigUtil;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

/**
 * Initialize zoo system properties and inject the Zoo config in Spring Boot bootstrap phase
 *
 * <p>Configuration example:</p>
 * <pre class="code">
 *   # set app.id
 *   app.id = 100004458
 *   # enable zoo bootstrap config and inject 'application' namespace in bootstrap phase
 *   zoo.bootstrap.enabled = true
 * </pre>
 *
 * or
 *
 * <pre class="code">
 *   # set app.id
 *   app.id = 100004458
 *   # enable zoo bootstrap config
 *   zoo.bootstrap.enabled = true
 *   # will inject 'application' and 'FX.zoo' namespaces in bootstrap phase
 *   zoo.bootstrap.namespaces = application,FX.zoo
 * </pre>
 *
 *
 * If you want to load Zoo configurations even before Logging System Initialization Phase,
 *  add
 * <pre class="code">
 *   # set zoo.bootstrap.eagerLoad.enabled
 *   zoo.bootstrap.eagerLoad.enabled = true
 * </pre>
 *
 *  This would be very helpful when your logging configurations is set by Zoo.
 *
 *  for example, you have defined logback-spring.xml in your project, and you want to inject some attributes into logback-spring.xml.
 *
 */
public class ZooApplicationContextInitializer implements
    ApplicationContextInitializer<ConfigurableApplicationContext> , EnvironmentPostProcessor, Ordered {
  public static final int DEFAULT_ORDER = 0;

  private static final Logger logger = LoggerFactory.getLogger(ZooApplicationContextInitializer.class);
  private static final Splitter NAMESPACE_SPLITTER = Splitter.on(",").omitEmptyStrings()
      .trimResults();
  public static final String[] ZOO_SYSTEM_PROPERTIES = {ZooClientSystemConsts.APP_ID,
      ZooClientSystemConsts.ZOO_LABEL,
      ZooClientSystemConsts.ZOO_CLUSTER,
      ZooClientSystemConsts.ZOO_CACHE_DIR,
      ZooClientSystemConsts.ZOO_ACCESS_KEY_SECRET,
      ZooClientSystemConsts.ZOO_META,
      ZooClientSystemConsts.ZOO_CONFIG_SERVICE,
      ZooClientSystemConsts.ZOO_PROPERTY_ORDER_ENABLE,
      ZooClientSystemConsts.ZOO_PROPERTY_NAMES_CACHE_ENABLE,
      ZooClientSystemConsts.ZOO_OVERRIDE_SYSTEM_PROPERTIES};

  private final ConfigPropertySourceFactory configPropertySourceFactory = SpringInjector
      .getInstance(ConfigPropertySourceFactory.class);

  private int order = DEFAULT_ORDER;

  @Override
  public void initialize(ConfigurableApplicationContext context) {
    ConfigurableEnvironment environment = context.getEnvironment();

    if (!environment.getProperty(PropertySourcesConstants.ZOO_BOOTSTRAP_ENABLED, Boolean.class, false)) {
      logger.debug("Zoo bootstrap config is not enabled for context {}, see property: ${{}}", context, PropertySourcesConstants.ZOO_BOOTSTRAP_ENABLED);
      return;
    }
    logger.debug("Zoo bootstrap config is enabled for context {}", context);

    initialize(environment);
  }


  /**
   * Initialize Zoo Configurations Just after environment is ready.
   *
   * @param environment
   */
  protected void initialize(ConfigurableEnvironment environment) {
    final ConfigUtil configUtil = ZooInjector.getInstance(ConfigUtil.class);
    if (environment.getPropertySources().contains(PropertySourcesConstants.ZOO_BOOTSTRAP_PROPERTY_SOURCE_NAME)) {
      //already initialized, replay the logs that were printed before the logging system was initialized
      DeferredLogger.replayTo();
      if (configUtil.isOverrideSystemProperties()) {
        // ensure ZooBootstrapPropertySources is still the first
        PropertySourcesUtil.ensureBootstrapPropertyPrecedence(environment);
      }
      return;
    }

    String namespaces = environment.getProperty(PropertySourcesConstants.ZOO_BOOTSTRAP_NAMESPACES, ConfigConsts.NAMESPACE_APPLICATION);
    logger.debug("Zoo bootstrap namespaces: {}", namespaces);
    List<String> namespaceList = NAMESPACE_SPLITTER.splitToList(namespaces);

    CompositePropertySource composite;
    if (configUtil.isPropertyNamesCacheEnabled()) {
      composite = new CachedCompositePropertySource(PropertySourcesConstants.ZOO_BOOTSTRAP_PROPERTY_SOURCE_NAME);
    } else {
      composite = new CompositePropertySource(PropertySourcesConstants.ZOO_BOOTSTRAP_PROPERTY_SOURCE_NAME);
    }
    for (String namespace : namespaceList) {
      Datum config = ConfigService.getConfig(namespace);

      composite.addPropertySource(configPropertySourceFactory.getConfigPropertySource(namespace, config));
    }
    if (!configUtil.isOverrideSystemProperties()) {
      if (environment.getPropertySources().contains(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)) {
        environment.getPropertySources().addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, composite);
        return;
      }
    }
    environment.getPropertySources().addFirst(composite);
  }

  /**
   * To fill system properties from environment config
   */
  void initializeSystemProperty(ConfigurableEnvironment environment) {
    for (String propertyName : ZOO_SYSTEM_PROPERTIES) {
      fillSystemPropertyFromEnvironment(environment, propertyName);
    }
  }

  private void fillSystemPropertyFromEnvironment(ConfigurableEnvironment environment, String propertyName) {
    if (System.getProperty(propertyName) != null) {
      return;
    }

    String propertyValue = environment.getProperty(propertyName);

    if (Strings.isNullOrEmpty(propertyValue)) {
      return;
    }

    System.setProperty(propertyName, propertyValue);
  }

  /**
   *
   * In order to load Zoo configurations as early as even before Spring loading logging system phase,
   * this EnvironmentPostProcessor can be called Just After ConfigFileApplicationListener has succeeded.
   *
   * <br />
   * The processing sequence would be like this: <br />
   * Load Bootstrap properties and application properties -----> load Zoo configuration properties ----> Initialize Logging systems
   *
   * @param configurableEnvironment
   * @param springApplication
   */
  @Override
  public void postProcessEnvironment(ConfigurableEnvironment configurableEnvironment, SpringApplication springApplication) {

    // should always initialize system properties like app.id in the first place
    initializeSystemProperty(configurableEnvironment);

    Boolean eagerLoadEnabled = configurableEnvironment.getProperty(PropertySourcesConstants.ZOO_BOOTSTRAP_EAGER_LOAD_ENABLED, Boolean.class, false);

    //EnvironmentPostProcessor should not be triggered if you don't want Zoo Loading before Logging System Initialization
    if (!eagerLoadEnabled) {
      return;
    }

    Boolean bootstrapEnabled = configurableEnvironment.getProperty(PropertySourcesConstants.ZOO_BOOTSTRAP_ENABLED, Boolean.class, false);

    if (bootstrapEnabled) {
      DeferredLogger.enable();
      initialize(configurableEnvironment);
    }

  }

  /**
   * @since 1.3.0
   */
  @Override
  public int getOrder() {
    return order;
  }

  /**
   * @since 1.3.0
   */
  public void setOrder(int order) {
    this.order = order;
  }

}
