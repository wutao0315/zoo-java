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
package com.wxius.framework.zoo.config.data.system;

import com.wxius.framework.zoo.config.data.util.Slf4jLogMessageFormatter;
import com.wxius.framework.zoo.spring.boot.ZooApplicationContextInitializer;
import org.apache.commons.logging.Log;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.util.StringUtils;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class ZooClientSystemPropertyInitializer {

  private final Log log;

  public ZooClientSystemPropertyInitializer(DeferredLogFactory logFactory) {
    this.log = logFactory.getLog(ZooClientSystemPropertyInitializer.class);
  }

  public void initializeSystemProperty(Binder binder, BindHandler bindHandler) {
    for (String propertyName : ZooApplicationContextInitializer.ZOO_SYSTEM_PROPERTIES) {
      this.fillSystemPropertyFromBinder(propertyName, propertyName, binder, bindHandler);
    }
  }

  private void fillSystemPropertyFromBinder(String propertyName, String bindName, Binder binder,
      BindHandler bindHandler) {
    if (System.getProperty(propertyName) != null) {
      return;
    }
    String propertyValue = binder.bind(bindName, Bindable.of(String.class), bindHandler)
        .orElse(null);
    if (!StringUtils.hasText(propertyValue)) {
      return;
    }
    log.debug(Slf4jLogMessageFormatter
        .format("zoo client set system property key=[{}] value=[{}]", propertyName,
            propertyValue));
    System.setProperty(propertyName, propertyValue);
  }
}
