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
package com.wxius.framework.zoo.config.data.internals;

import com.wxius.framework.zoo.infrastructure.DatumRepository;
import com.wxius.framework.zoo.infrastructure.DefaultDatum;
import com.wxius.framework.zoo.infrastructure.LocalRepository;
import com.wxius.framework.zoo.infrastructure.RepositoryChangeListener;
import com.google.common.collect.Sets;
import java.util.Set;
import org.springframework.util.CollectionUtils;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class PureZooConfig extends DefaultDatum implements RepositoryChangeListener {

  /**
   * Constructor.
   *
   * @param name        the namespace of this config instance
   * @param configRepository the config repository for this config instance
   */
  public PureZooConfig(String name,
      LocalRepository configRepository) {
    super(name, configRepository);
  }

  @Override
  public String getProperty(String key, String defaultValue) {
    // step 1: check local cached properties file
    String value = this.getPropertyFromRepository(key);

    // step 2: check properties file from classpath
    if (value == null) {
      value = this.getPropertyFromAdditional(key);
    }

    this.tryWarnLog(value);

    return value == null ? defaultValue : value;
  }

  @Override
  public Set<String> getPropertyNames() {
    // pure zoo config only contains the property from repository and the property from additional
    Set<String> fromRepository = this.getPropertyNamesFromRepository();
    Set<String> fromAdditional = this.getPropertyNamesFromAdditional();
    if (CollectionUtils.isEmpty(fromRepository)) {
      return fromAdditional;
    }
    if (CollectionUtils.isEmpty(fromAdditional)) {
      return fromRepository;
    }
    Set<String> propertyNames = Sets
        .newLinkedHashSetWithExpectedSize(fromRepository.size() + fromAdditional.size());
    propertyNames.addAll(fromRepository);
    propertyNames.addAll(fromAdditional);
    return propertyNames;
  }
}
