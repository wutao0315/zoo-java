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

import com.wxius.framework.zoo.Datum;
import com.wxius.framework.zoo.ConfigService;
import com.wxius.framework.zoo.config.data.util.Slf4jLogMessageFormatter;
import com.wxius.framework.zoo.spring.config.ConfigPropertySource;
import com.wxius.framework.zoo.spring.config.ConfigPropertySourceFactory;
import com.wxius.framework.zoo.spring.util.SpringInjector;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.springframework.boot.BootstrapRegistry.InstanceSupplier;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigDataLoader;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.PropertySource;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class ZooConfigDataLoader implements ConfigDataLoader<ZooConfigDataResource>, Ordered {

  private final DeferredLogFactory logFactory;

  private final Log log;

  public ZooConfigDataLoader(DeferredLogFactory logFactory) {
    this.logFactory = logFactory;
    this.log = logFactory.getLog(ZooConfigDataLoader.class);
  }

  @Override
  public ConfigData load(ConfigDataLoaderContext context, ZooConfigDataResource resource)
      throws IOException, ConfigDataResourceNotFoundException {
    ConfigurableBootstrapContext bootstrapContext = context.getBootstrapContext();
    Binder binder = bootstrapContext.get(Binder.class);
    BindHandler bindHandler = this.getBindHandler(context);
    bootstrapContext.registerIfAbsent(ZooConfigDataLoaderInitializer.class, InstanceSupplier
        .from(() -> new ZooConfigDataLoaderInitializer(this.logFactory, binder, bindHandler,
            bootstrapContext)));
    ZooConfigDataLoaderInitializer zooConfigDataLoaderInitializer = bootstrapContext
        .get(ZooConfigDataLoaderInitializer.class);
    // init zoo client
    List<PropertySource<?>> initialPropertySourceList = zooConfigDataLoaderInitializer
        .initZooClient();
    // load config
    bootstrapContext.registerIfAbsent(ConfigPropertySourceFactory.class,
        InstanceSupplier.from(() -> SpringInjector.getInstance(ConfigPropertySourceFactory.class)));
    ConfigPropertySourceFactory configPropertySourceFactory = bootstrapContext
        .get(ConfigPropertySourceFactory.class);
    String namespace = resource.getNamespace();
    Datum config = ConfigService.getConfig(namespace);
    ConfigPropertySource configPropertySource = configPropertySourceFactory
        .getConfigPropertySource(namespace, config);
    List<PropertySource<?>> propertySourceList = new ArrayList<>();
    propertySourceList.add(configPropertySource);
    propertySourceList.addAll(initialPropertySourceList);
    log.debug(Slf4jLogMessageFormatter.format("zoo client loaded namespace [{}]", namespace));
    return new ConfigData(propertySourceList);
  }

  private BindHandler getBindHandler(ConfigDataLoaderContext context) {
    return context.getBootstrapContext().getOrElse(BindHandler.class, null);
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 100;
  }
}
