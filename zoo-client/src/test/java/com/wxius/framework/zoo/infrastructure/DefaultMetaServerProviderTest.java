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

import com.wxius.framework.zoo.core.ConfigConsts;
import com.wxius.framework.zoo.core.enums.Env;
//import com.wxius.framework.zoo.infrastructure.DefaultMetaServerProvider;
import org.junit.After;
import org.junit.Test;

public class DefaultMetaServerProviderTest {

  @After
  public void tearDown() throws Exception {
    System.clearProperty(ConfigConsts.ZOO_META_KEY);
  }

  @Test
  public void testWithSystemProperty() throws Exception {
    String someMetaAddress = "someMetaAddress";
    Env someEnv = Env.DEV;

    System.setProperty(ConfigConsts.ZOO_META_KEY, " " + someMetaAddress + " ");

//    DefaultMetaServerProvider defaultMetaServerProvider = new DefaultMetaServerProvider();

//    assertEquals(someMetaAddress, defaultMetaServerProvider.getMetaServerAddress(someEnv));
  }

}
