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
package com.wxius.framework.zoo.config.data.importer;

import com.wxius.framework.zoo.config.data.util.Slf4jLogMessageFormatter;
import com.wxius.framework.zoo.core.ConfigConsts;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.springframework.boot.context.config.ConfigDataLocation;
import org.springframework.boot.context.config.ConfigDataLocationNotFoundException;
import org.springframework.boot.context.config.ConfigDataLocationResolver;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class ZooConfigDataLocationResolver implements
    ConfigDataLocationResolver<ZooConfigDataResource>, Ordered {

  private static final String PREFIX = "zoo://";

  private final Log log;

  public ZooConfigDataLocationResolver(DeferredLogFactory logFactory) {
    this.log = logFactory.getLog(ZooConfigDataLocationResolver.class);
  }

  @Override
  public boolean isResolvable(ConfigDataLocationResolverContext context,
      ConfigDataLocation location) {
    return location.hasPrefix(PREFIX);
  }

  @Override
  public List<ZooConfigDataResource> resolve(ConfigDataLocationResolverContext context,
      ConfigDataLocation location)
      throws ConfigDataLocationNotFoundException, ConfigDataResourceNotFoundException {
    return Collections.emptyList();
  }

  @Override
  public List<ZooConfigDataResource> resolveProfileSpecific(
      ConfigDataLocationResolverContext context, ConfigDataLocation location, Profiles profiles)
      throws ConfigDataLocationNotFoundException {
    String namespace = location.getNonPrefixedValue(PREFIX);
    if (StringUtils.hasText(namespace)) {
      log.debug(Slf4jLogMessageFormatter.format("zoo config namespace [{}]", namespace));
      return Collections.singletonList(new ZooConfigDataResource(namespace));
    }
    log.debug(Slf4jLogMessageFormatter.format("zoo config namespace is empty, default to [{}]",
        ConfigConsts.NAMESPACE_APPLICATION));
    return Collections.singletonList(ZooConfigDataResource.DEFAULT);
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 100;
  }
}
