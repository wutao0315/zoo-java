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

import com.wxius.framework.zoo.DatumChangeListener;
import java.util.Set;

import org.springframework.core.env.EnumerablePropertySource;

import com.wxius.framework.zoo.Datum;

/**
 * Property source wrapper for Config
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigPropertySource extends EnumerablePropertySource<Datum> {
  private static final String[] EMPTY_ARRAY = new String[0];

  ConfigPropertySource(String name, Datum source) {
    super(name, source);
  }

  @Override
  public boolean containsProperty(String name) {
    return this.source.getProperty(name, null) != null;
  }

  @Override
  public String[] getPropertyNames() {
    Set<String> propertyNames = this.source.getPropertyNames();
    if (propertyNames.isEmpty()) {
      return EMPTY_ARRAY;
    }
    return propertyNames.toArray(new String[propertyNames.size()]);
  }

  @Override
  public Object getProperty(String name) {
    return this.source.getProperty(name, null);
  }

  public void addChangeListener(DatumChangeListener listener) {
    this.source.addChangeListener(listener);
  }
}
