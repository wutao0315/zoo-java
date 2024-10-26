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
package com.wxius.framework.zoo.core;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class ZooClientSystemConsts {

  /**
   * zoo client app id
   */
  public static final String APP_ID = "app.id";

  /**
   * zoo client app label
   */
  public static final String ZOO_LABEL = "zoo.label";

  /**
   * zoo client app id environment variables
   */
  public static final String APP_ID_ENVIRONMENT_VARIABLES = "APP_ID";

  /**
   * zoo client app label environment variables
   */
  public static final String ZOO_LABEL_ENVIRONMENT_VARIABLES = "ZOO_LABEL";

  /**
   * cluster name
   */
  public static final String ZOO_CLUSTER = ConfigConsts.ZOO_CLUSTER_KEY;

  /**
   * cluster name environment variables
   */
  public static final String ZOO_CLUSTER_ENVIRONMENT_VARIABLES = "ZOO_CLUSTER";

  /**
   * local cache directory
   */
  public static final String ZOO_CACHE_DIR = "zoo.cache-dir";

  /**
   * local cache directory
   */
  @Deprecated
  public static final String DEPRECATED_ZOO_CACHE_DIR = "zoo.cacheDir";

  /**
   * local cache directory environment variables
   */
  public static final String ZOO_CACHE_DIR_ENVIRONMENT_VARIABLES = "ZOO_CACHE_DIR";

  /**
   * local cache directory environment variables
   */
  @Deprecated
  public static final String DEPRECATED_ZOO_CACHE_DIR_ENVIRONMENT_VARIABLES = "ZOO_CACHEDIR";

  /**
   * zoo client access key
   */
  public static final String ZOO_ACCESS_KEY_SECRET = "zoo.access-key.secret";

  /**
   * zoo client access key
   */
  @Deprecated
  public static final String DEPRECATED_ZOO_ACCESS_KEY_SECRET = "zoo.accesskey.secret";

  /**
   * zoo client access key environment variables
   */
  public static final String ZOO_ACCESS_KEY_SECRET_ENVIRONMENT_VARIABLES = "ZOO_ACCESS_KEY_SECRET";

  /**
   * zoo client access key environment variables
   */
  @Deprecated
  public static final String DEPRECATED_ZOO_ACCESS_KEY_SECRET_ENVIRONMENT_VARIABLES = "ZOO_ACCESSKEY_SECRET";

  /**
   * zoo meta server address
   */
  public static final String ZOO_META = ConfigConsts.ZOO_META_KEY;

  /**
   * zoo meta server address environment variables
   */
  public static final String ZOO_META_ENVIRONMENT_VARIABLES = "ZOO_META";

  /**
   * zoo config service address
   */
  public static final String ZOO_CONFIG_SERVICE = "zoo.config-service";

  /**
   * zoo config service address
   */
  @Deprecated
  public static final String DEPRECATED_ZOO_CONFIG_SERVICE = "zoo.configService";

  /**
   * zoo config service address environment variables
   */
  public static final String ZOO_CONFIG_SERVICE_ENVIRONMENT_VARIABLES = "ZOO_CONFIG_SERVICE";

  /**
   * zoo config service address environment variables
   */
  @Deprecated
  public static final String DEPRECATED_ZOO_CONFIG_SERVICE_ENVIRONMENT_VARIABLES = "ZOO_CONFIGSERVICE";

  /**
   * enable property order
   */
  public static final String ZOO_PROPERTY_ORDER_ENABLE = "zoo.property.order.enable";

  /**
   * enable property order environment variables
   */
  public static final String ZOO_PROPERTY_ORDER_ENABLE_ENVIRONMENT_VARIABLES = "ZOO_PROPERTY_ORDER_ENABLE";

  /**
   * enable property names cache
   */
  public static final String ZOO_PROPERTY_NAMES_CACHE_ENABLE = "zoo.property.names.cache.enable";

  /**
   * enable property names cache environment variables
   */
  public static final String ZOO_PROPERTY_NAMES_CACHE_ENABLE_ENVIRONMENT_VARIABLES = "ZOO_PROPERTY_NAMES_CACHE_ENABLE";

  /**
   * enable property names cache
   */
  public static final String ZOO_CACHE_FILE_ENABLE = "zoo.cache.file.enable";

  /**
   * enable property names cache environment variables
   */
  public static final String ZOO_CACHE_FILE_ENABLE_ENVIRONMENT_VARIABLES = "ZOO_CACHE_FILE_ENABLE";

  /**
   * enable zoo overrideSystemProperties
   */
  public static final String ZOO_OVERRIDE_SYSTEM_PROPERTIES = "zoo.override-system-properties";

  /**
   * enable zoo encrypt
   */
  public static final String ZOO_ENCRYPT_ENABLE = "zoo.encrypt-properties";

}
