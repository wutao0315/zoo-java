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
package com.wxius.framework.zoo.config.data.extension.initialize;

import com.wxius.framework.zoo.config.data.extension.enums.ZooClientMessagingType;
import com.wxius.framework.zoo.config.data.extension.properties.ZooClientExtensionProperties;
import com.wxius.framework.zoo.config.data.extension.properties.ZooClientProperties;
import com.wxius.framework.zoo.config.data.extension.webclient.ZooClientLongPollingExtensionInitializer;
import com.wxius.framework.zoo.config.data.extension.websocket.ZooClientWebsocketExtensionInitializer;
import com.wxius.framework.zoo.config.data.util.Slf4jLogMessageFormatter;
import org.apache.commons.logging.Log;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.logging.DeferredLogFactory;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class ZooClientExtensionInitializeFactory {

  private final Log log;

  private final ZooClientPropertiesFactory zooClientPropertiesFactory;

  private final ZooClientLongPollingExtensionInitializer zooClientLongPollingExtensionInitializer;

  private final ZooClientWebsocketExtensionInitializer zooClientWebsocketExtensionInitializer;

  public ZooClientExtensionInitializeFactory(DeferredLogFactory logFactory,
      ConfigurableBootstrapContext bootstrapContext) {
    this.log = logFactory.getLog(ZooClientExtensionInitializeFactory.class);
    this.zooClientPropertiesFactory = new ZooClientPropertiesFactory();
    this.zooClientLongPollingExtensionInitializer = new ZooClientLongPollingExtensionInitializer(
        logFactory,
        bootstrapContext);
    this.zooClientWebsocketExtensionInitializer = new ZooClientWebsocketExtensionInitializer(
        logFactory,
        bootstrapContext);
  }

  /**
   * initialize extension
   *
   * @param binder      properties binder
   * @param bindHandler properties bind handler
   */
  public void initializeExtension(Binder binder, BindHandler bindHandler) {
    ZooClientProperties zooClientProperties = this.zooClientPropertiesFactory
        .createZooClientProperties(binder, bindHandler);
    if (zooClientProperties == null || zooClientProperties.getExtension() == null) {
      this.log.info("zoo client extension is not configured, default to disabled");
      return;
    }
    ZooClientExtensionProperties extension = zooClientProperties.getExtension();
    if (!extension.getEnabled()) {
      this.log.info("zoo client extension disabled");
      return;
    }
    ZooClientMessagingType messagingType = extension.getMessagingType();
    log.debug(Slf4jLogMessageFormatter
        .format("zoo client extension messaging type: {}", messagingType));
    switch (messagingType) {
      case LONG_POLLING:
        this.zooClientLongPollingExtensionInitializer
            .initialize(zooClientProperties, binder, bindHandler);
        return;
      case WEBSOCKET:
        this.zooClientWebsocketExtensionInitializer
            .initialize(zooClientProperties, binder, bindHandler);
        return;
      default:
        throw new IllegalStateException("Unexpected value: " + messagingType);
    }
  }
}
