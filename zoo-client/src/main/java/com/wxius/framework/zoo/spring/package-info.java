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
/**
 * This package contains Zoo Spring integration codes and enables the following features:<br/>
 * <p>1. Support Spring XML based configuration</p>
 * <ul>
 *   <li>&lt;zoo:config namespaces="someNamespace"/&gt; to inject configurations from Zoo into Spring Property
 *   Sources so that placeholders like ${someProperty} and @Value("someProperty") are supported.</li>
 * </ul>
 * <p>2. Support Spring Java based configuration</p>
 * <ul>
 *   <li>@EnableZooConfig(namespaces={"someNamespace"}) to inject configurations from Zoo into Spring Property
 *   Sources so that placeholders like ${someProperty} and @Value("someProperty") are supported.</li>
 * </ul>
 *
 * With the above configuration, annotations like @ZooConfig("someNamespace")
 * and @ZooConfigChangeListener("someNamespace) are also supported.<br />
 * <br />
 * Requires Spring 3.1.1+
 */
package com.wxius.framework.zoo.spring;