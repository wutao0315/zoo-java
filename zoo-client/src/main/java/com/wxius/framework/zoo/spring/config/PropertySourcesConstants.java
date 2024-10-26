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
package com.wxius.framework.zoo.spring.config;

public interface PropertySourcesConstants {
  String ZOO_PROPERTY_SOURCE_NAME = "ZooPropertySources";
  String ZOO_BOOTSTRAP_PROPERTY_SOURCE_NAME = "ZooBootstrapPropertySources";
  String ZOO_BOOTSTRAP_ENABLED = "zoo.bootstrap.enabled";
  String ZOO_BOOTSTRAP_EAGER_LOAD_ENABLED = "zoo.bootstrap.eagerLoad.enabled";
  String ZOO_BOOTSTRAP_NAMESPACES = "zoo.bootstrap.namespaces";
}
