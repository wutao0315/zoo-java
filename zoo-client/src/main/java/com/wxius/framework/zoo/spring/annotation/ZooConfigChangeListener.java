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
package com.wxius.framework.zoo.spring.annotation;

import com.wxius.framework.zoo.core.ConfigConsts;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to register Zoo ConfigChangeListener.
 *
 * <p>Usage example:</p>
 * <pre class="code">
 * //Listener on namespaces of "someNamespace" and "anotherNamespace", will be notified when any key is changed
 * &#064;ZooConfigChangeListener({"someNamespace","anotherNamespace"})
 * private void onChange(ConfigChangeEvent changeEvent) {
 *     //handle change event
 * }
 * <br />
 * //The namespace could also be specified as a placeholder, e.g. ${redis.namespace:xxx}, which will use the value of the key "redis.namespace" or "xxx" if this key is not configured.
 * &#064;ZooConfigChangeListener({"${redis.namespace:xxx}"})
 * private void onChange(ConfigChangeEvent changeEvent) {
 *     //handle change event
 * }
 * <br />
 *  //The namespace could also be specified as a placeholder for a comma separated namespaces, e.g. ${app.allNamespaces}, where "all.allNamespaces" = "someNamespace,anotherNamespace,yetAnotherNamespace"
 *  &#064;ZooConfigChangeListener({"${app.allNamespaces}"})
 *  private void onChange(ConfigChangeEvent changeEvent) {
 *      //handle change event
 *  }
 *  <br />
 * //Listener on namespaces of "someNamespace" and "anotherNamespace", will only be notified when "someKey" or "anotherKey" is changed
 * &#064;ZooConfigChangeListener(value = {"someNamespace","anotherNamespace"}, interestedKeys = {"someKey", "anotherKey"})
 * private void onChange(ConfigChangeEvent changeEvent) {
 *     //handle change event
 * }
 * </pre>
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ZooConfigChangeListener {
  /**
   * Zoo namespace for the config, if not specified then default to application
   */
  String[] value() default {ConfigConsts.NAMESPACE_APPLICATION};

  /**
   * The keys interested by the listener, will only be notified if any of the interested keys is changed.
   * <br />
   * If neither of {@code interestedKeys} and {@code interestedKeyPrefixes} is specified then the {@code listener} will be notified when any key is changed.
   */
  String[] interestedKeys() default {};

  /**
   * The key prefixes that the listener is interested in, will be notified if and only if the changed keys start with anyone of the prefixes.
   * The prefixes will simply be used to determine whether the {@code listener} should be notified or not using {@code changedKey.startsWith(prefix)}.
   * e.g. "spring." means that {@code listener} is interested in keys that starts with "spring.", such as "spring.banner", "spring.jpa", etc.
   * and "application" means that {@code listener} is interested in keys that starts with "application", such as "applicationName", "application.port", etc.
   * <br />
   * If neither of {@code interestedKeys} and {@code interestedKeyPrefixes} is specified then the {@code listener} will be notified when whatever key is changed.
   */
  String[] interestedKeyPrefixes() default {};
}
