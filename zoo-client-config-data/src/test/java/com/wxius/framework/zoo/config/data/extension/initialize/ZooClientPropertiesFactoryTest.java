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
import com.wxius.framework.zoo.config.data.extension.properties.ZooClientProperties;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class ZooClientPropertiesFactoryTest {

  @Test
  public void testCreateZooClientProperties() throws IOException {
    Map<String, String> map = new LinkedHashMap<>();
    map.put("zoo.client.extension.enabled", "true");
    map.put("zoo.client.extension.messaging-type", "long_polling");
    MapConfigurationPropertySource propertySource = new MapConfigurationPropertySource(map);
    Binder binder = new Binder(propertySource);
    ZooClientPropertiesFactory factory = new ZooClientPropertiesFactory();
    ZooClientProperties zooClientProperties = factory
        .createZooClientProperties(binder, null);

    Assert.assertEquals(zooClientProperties.getExtension().getEnabled(), true);
    Assert.assertEquals(zooClientProperties.getExtension().getMessagingType(),
        ZooClientMessagingType.LONG_POLLING);
  }
}
