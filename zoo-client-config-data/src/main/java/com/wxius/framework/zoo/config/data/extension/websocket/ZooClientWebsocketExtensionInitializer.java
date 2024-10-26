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
package com.wxius.framework.zoo.config.data.extension.websocket;

import com.wxius.framework.zoo.config.data.extension.initialize.ZooClientExtensionInitializer;
import com.wxius.framework.zoo.config.data.extension.properties.ZooClientProperties;
import org.apache.commons.logging.Log;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.logging.DeferredLogFactory;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class ZooClientWebsocketExtensionInitializer implements ZooClientExtensionInitializer {

  private final Log log;

  private final ConfigurableBootstrapContext bootstrapContext;

  public ZooClientWebsocketExtensionInitializer(DeferredLogFactory logFactory,
      ConfigurableBootstrapContext bootstrapContext) {
    this.log = logFactory.getLog(ZooClientWebsocketExtensionInitializer.class);
    this.bootstrapContext = bootstrapContext;
  }

  @Override
  public void initialize(ZooClientProperties zooClientProperties, Binder binder,
      BindHandler bindHandler) {
    throw new UnsupportedOperationException("zoo client websocket support is not complete yet.");
  }
}
