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

import com.wxius.framework.zoo.config.data.extension.properties.ZooClientProperties;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Binder;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public interface ZooClientExtensionInitializer {

  /**
   * initialize extension
   *
   * @param zooClientProperties zoo client extension properties
   * @param binder                 properties binder
   * @param bindHandler            properties bind handler
   */
  void initialize(ZooClientProperties zooClientProperties, Binder binder,
      BindHandler bindHandler);
}
