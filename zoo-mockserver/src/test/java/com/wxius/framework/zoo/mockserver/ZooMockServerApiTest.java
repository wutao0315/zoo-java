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
package com.wxius.framework.zoo.mockserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.wxius.framework.zoo.Datum;
import com.wxius.framework.zoo.DatumChangeListener;
import com.wxius.framework.zoo.ConfigService;
import com.wxius.framework.zoo.model.ConfigChangeEvent;
import com.google.common.util.concurrent.SettableFuture;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.ClassRule;
import org.junit.Test;

public class ZooMockServerApiTest {

  private static final String anotherNamespace = "anotherNamespace";

  @ClassRule
  public static EmbeddedZoo embeddedZoo = new EmbeddedZoo();

  @Test
  public void testGetProperty() throws Exception {
    Datum applicationConfig = ConfigService.getAppConfig();

    assertEquals("value1", applicationConfig.getProperty("key1", null));
    assertEquals("value2", applicationConfig.getProperty("key2", null));
  }

  @Test
  public void testUpdateProperties() throws Exception {
    String someNewValue = "someNewValue";

    Datum otherConfig = ConfigService.getConfig(anotherNamespace);

    final SettableFuture<ConfigChangeEvent> future = SettableFuture.create();

    otherConfig.addChangeListener(new DatumChangeListener() {
      @Override
      public void onChange(ConfigChangeEvent changeEvent) {
        future.set(changeEvent);
      }
    });

    assertEquals("otherValue1", otherConfig.getProperty("key1", null));
    assertEquals("otherValue2", otherConfig.getProperty("key2", null));

    embeddedZoo.addOrModifyProperty(anotherNamespace, "key1", someNewValue);

    ConfigChangeEvent changeEvent = future.get(5, TimeUnit.SECONDS);

    assertEquals(someNewValue, otherConfig.getProperty("key1", null));
    assertEquals("otherValue2", otherConfig.getProperty("key2", null));
    assertTrue(changeEvent.isChanged("key1"));
  }

  @Test
  public void testUpdateSamePropertyTwice() throws Exception {
    String someNewValue = "someNewValue";

    Datum otherConfig = ConfigService.getConfig(anotherNamespace);

    final Semaphore changes = new Semaphore(0);

    otherConfig.addChangeListener(new DatumChangeListener() {
      @Override
      public void onChange(ConfigChangeEvent changeEvent) {
        changes.release();
      }
    });

    assertEquals("otherValue3", otherConfig.getProperty("key3", null));

    embeddedZoo.addOrModifyProperty(anotherNamespace, "key3", someNewValue);
    embeddedZoo.addOrModifyProperty(anotherNamespace, "key3", someNewValue);

    assertTrue(changes.tryAcquire(5, TimeUnit.SECONDS));
    assertEquals(someNewValue, otherConfig.getProperty("key3", null));
    assertEquals(0, changes.availablePermits());
  }

  @Test
  public void testDeleteProperties() throws Exception {
    Datum otherConfig = ConfigService.getConfig(anotherNamespace);

    final SettableFuture<ConfigChangeEvent> future = SettableFuture.create();

    otherConfig.addChangeListener(new DatumChangeListener() {
      @Override
      public void onChange(ConfigChangeEvent changeEvent) {
        future.set(changeEvent);
      }
    });

    assertEquals("otherValue4", otherConfig.getProperty("key4", null));
    assertEquals("otherValue5", otherConfig.getProperty("key5", null));

    embeddedZoo.deleteProperty(anotherNamespace, "key4");

    ConfigChangeEvent changeEvent = future.get(5, TimeUnit.SECONDS);

    assertNull(otherConfig.getProperty("key4", null));
    assertEquals("otherValue5", otherConfig.getProperty("key5", null));
    assertTrue(changeEvent.isChanged("key4"));
  }

  @Test
  public void testDeleteSamePropertyTwice() throws Exception {
    Datum otherConfig = ConfigService.getConfig(anotherNamespace);

    final Semaphore changes = new Semaphore(0);

    otherConfig.addChangeListener(new DatumChangeListener() {
      @Override
      public void onChange(ConfigChangeEvent changeEvent) {
        changes.release();
      }
    });

    assertEquals("otherValue6", otherConfig.getProperty("key6", null));

    embeddedZoo.deleteProperty(anotherNamespace, "key6");
    embeddedZoo.deleteProperty(anotherNamespace, "key6");

    assertTrue(changes.tryAcquire(5, TimeUnit.SECONDS));
    assertNull(otherConfig.getProperty("key6", null));
    assertEquals(0, changes.availablePermits());
  }
}
