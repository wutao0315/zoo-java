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
package com.wxius.framework.zoo.infrastructure;

import static org.junit.Assert.assertEquals;

import com.wxius.framework.zoo.core.ZooClientSystemConsts;
import com.wxius.framework.zoo.core.dto.ServiceDTO;
import java.util.List;

import org.junit.After;
import org.junit.Test;

public class ConfigServiceLocatorTest {

  @After
  public void tearDown() throws Exception {
    System.clearProperty(ZooClientSystemConsts.ZOO_CONFIG_SERVICE);
    System.clearProperty(ZooClientSystemConsts.DEPRECATED_ZOO_CONFIG_SERVICE);
  }

  @Test
  public void testGetConfigServicesWithSystemProperty() throws Exception {
    String someConfigServiceUrl = " someConfigServiceUrl ";
    String anotherConfigServiceUrl = " anotherConfigServiceUrl ";

    System.setProperty(ZooClientSystemConsts.ZOO_CONFIG_SERVICE, someConfigServiceUrl + "," + anotherConfigServiceUrl);

//    ConfigServiceLocator configServiceLocator = new ConfigServiceLocator();
//
//    List<ServiceDTO> result = configServiceLocator.getConfigServices();
//
//    assertEquals(2, result.size());
//
//    assertEquals(someConfigServiceUrl.trim(), result.get(0).getHomepageUrl());
//    assertEquals(anotherConfigServiceUrl.trim(), result.get(1).getHomepageUrl());
  }

  @Test
  public void testGetConfigServicesWithSystemPropertyCompatible() throws Exception {
    String someConfigServiceUrl = " someConfigServiceUrl ";
    String anotherConfigServiceUrl = " anotherConfigServiceUrl ";

    System.setProperty(ZooClientSystemConsts.DEPRECATED_ZOO_CONFIG_SERVICE,
        someConfigServiceUrl + "," + anotherConfigServiceUrl);

//    ConfigServiceLocator configServiceLocator = new ConfigServiceLocator();
//
//    List<ServiceDTO> result = configServiceLocator.getConfigServices();
//
//    assertEquals(2, result.size());
//
//    assertEquals(someConfigServiceUrl.trim(), result.get(0).getHomepageUrl());
//    assertEquals(anotherConfigServiceUrl.trim(), result.get(1).getHomepageUrl());
  }
}
