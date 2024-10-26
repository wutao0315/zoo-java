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
package com.wxius.framework.zoo.config.data.extension.properties;

import com.wxius.framework.zoo.config.data.extension.enums.ZooClientMessagingType;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class ZooClientExtensionProperties {

  /**
   * enable zoo client extension(webclient/websocket and authentication)
   */
  private Boolean enabled = false;

  /**
   * zoo client listening type
   */
  private ZooClientMessagingType messagingType = ZooClientMessagingType.LONG_POLLING;

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public ZooClientMessagingType getMessagingType() {
    return messagingType;
  }

  public void setMessagingType(
      ZooClientMessagingType messagingType) {
    this.messagingType = messagingType;
  }

  @Override
  public String toString() {
    return "ZooClientExtensionProperties{" +
        "enabled=" + enabled +
        ", messagingType=" + messagingType +
        '}';
  }
}
