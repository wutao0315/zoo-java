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
package com.wxius.framework.zoo.spring.spi;

import static org.springframework.test.util.AssertionErrors.assertEquals;

import com.wxius.framework.zoo.spring.annotation.ZooConfigRegistrar;
import java.lang.reflect.Field;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

public class ZooConfigRegistrarHelperTest {

  @Test
  public void testHelperLoadingOrder() {
    ZooConfigRegistrar zooConfigRegistrar = new ZooConfigRegistrar();

    Field field = ReflectionUtils.findField(ZooConfigRegistrar.class, "helper");
    ReflectionUtils.makeAccessible(field);
    Object helper = ReflectionUtils.getField(field, zooConfigRegistrar);

    assertEquals("helper is not TestRegistrarHelper instance", TestRegistrarHelper.class, helper.getClass());
  }
}
