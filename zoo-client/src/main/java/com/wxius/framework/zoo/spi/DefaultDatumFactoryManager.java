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
package com.wxius.framework.zoo.spi;

import java.util.Map;

import com.wxius.framework.zoo.build.ZooInjector;
import com.google.common.collect.Maps;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class DefaultDatumFactoryManager implements DatumFactoryManager {
  private DatumRegistry m_registry;

  private Map<String, DatumFactory> m_factories = Maps.newConcurrentMap();

  public DefaultDatumFactoryManager() {
    m_registry = ZooInjector.getInstance(DatumRegistry.class);
  }

  @Override
  public DatumFactory getFactory(String name) {
    // step 1: check hacked factory
    DatumFactory factory = m_registry.getFactory(name);

    if (factory != null) {
      return factory;
    }

    // step 2: check cache
    factory = m_factories.get(name);

    if (factory != null) {
      return factory;
    }

    // step 3: check declared config factory
    factory = ZooInjector.getInstance(DatumFactory.class, name);

    if (factory != null) {
      return factory;
    }

    // step 4: check default config factory
    factory = ZooInjector.getInstance(DatumFactory.class);

    m_factories.put(name, factory);

    // factory should not be null
    return factory;
  }
}
