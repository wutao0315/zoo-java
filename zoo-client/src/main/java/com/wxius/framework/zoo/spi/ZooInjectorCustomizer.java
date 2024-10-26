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

import com.wxius.framework.zoo.core.spi.Ordered;
import com.wxius.framework.zoo.infrastructure.DefaultInjector;
import com.wxius.framework.zoo.infrastructure.Injector;

/**
 * Allow users to inject customized instances, see {@link DefaultInjector#getInstance(java.lang.Class)}
 */
public interface ZooInjectorCustomizer extends Injector, Ordered {

}
