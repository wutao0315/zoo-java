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
package com.wxius.framework.zoo.config.data.extension.webclient;

import com.wxius.framework.zoo.config.data.extension.initialize.ZooClientExtensionInitializer;
import com.wxius.framework.zoo.config.data.extension.properties.ZooClientProperties;
import com.wxius.framework.zoo.config.data.extension.webclient.customizer.spi.ZooClientWebClientCustomizerFactory;
import com.wxius.framework.zoo.config.data.injector.ZooConfigDataInjectorCustomizer;
import com.wxius.framework.zoo.util.http.HttpClient;
import com.wxius.framework.foundation.internals.ServiceBootstrap;
import java.util.List;
import org.apache.commons.logging.Log;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class ZooClientLongPollingExtensionInitializer implements
    ZooClientExtensionInitializer {

  private final Log log;

  private final ConfigurableBootstrapContext bootstrapContext;

  public ZooClientLongPollingExtensionInitializer(DeferredLogFactory logFactory,
      ConfigurableBootstrapContext bootstrapContext) {
    this.log = logFactory.getLog(ZooClientLongPollingExtensionInitializer.class);
    this.bootstrapContext = bootstrapContext;
  }

  @Override
  public void initialize(ZooClientProperties zooClientProperties, Binder binder,
      BindHandler bindHandler) {
    WebClient.Builder webClientBuilder = WebClient.builder();
    List<ZooClientWebClientCustomizerFactory> factories = ServiceBootstrap
        .loadAllOrdered(ZooClientWebClientCustomizerFactory.class);
    if (!CollectionUtils.isEmpty(factories)) {
      for (ZooClientWebClientCustomizerFactory factory : factories) {
        WebClientCustomizer webClientCustomizer = factory
            .createWebClientCustomizer(zooClientProperties, binder, bindHandler, this.log,
                this.bootstrapContext);
        if (webClientCustomizer != null) {
          webClientCustomizer.customize(webClientBuilder);
        }
      }
    }
    HttpClient httpClient = new ZooWebClientHttpClient(webClientBuilder.build());
    ZooConfigDataInjectorCustomizer.registerIfAbsent(HttpClient.class, () -> httpClient);
  }
}
