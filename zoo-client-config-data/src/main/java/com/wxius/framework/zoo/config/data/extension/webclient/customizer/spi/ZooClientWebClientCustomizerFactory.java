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
package com.wxius.framework.zoo.config.data.extension.webclient.customizer.spi;

import com.wxius.framework.zoo.config.data.extension.properties.ZooClientProperties;
import com.wxius.framework.zoo.core.spi.Ordered;
import org.apache.commons.logging.Log;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.lang.Nullable;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public interface ZooClientWebClientCustomizerFactory extends Ordered {

  /**
   * create a WebClientCustomizer instance
   *
   * @param zooClientProperties zoo client binded properties
   * @param binder                 properties binder
   * @param bindHandler            properties binder Handler
   * @param log                    deferred log
   * @param bootstrapContext       bootstrapContext
   * @return WebClientCustomizer instance or null
   */
  @Nullable
  WebClientCustomizer createWebClientCustomizer(ZooClientProperties zooClientProperties,
      Binder binder, BindHandler bindHandler, Log log,
      ConfigurableBootstrapContext bootstrapContext);
}
