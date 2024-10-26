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
package com.wxius.framework.zoo.tracer.internals;

import com.wxius.framework.zoo.tracer.spi.MessageProducer;
import com.wxius.framework.zoo.tracer.spi.MessageProducerManager;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class MockMessageProducerManager implements MessageProducerManager {
  private static MessageProducer s_producer;

  @Override
  public MessageProducer getProducer() {
    return s_producer;
  }

  public static void setProducer(MessageProducer producer) {
    s_producer = producer;
  }
}