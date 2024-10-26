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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wxius.framework.zoo.Datum;
import com.wxius.framework.zoo.DatumChangeListener;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class ConfigPropertySourceTest {

  private ConfigPropertySource configPropertySource;

  @Mock
  private Datum someConfig;

  @Before
  public void setUp() throws Exception {
    String someName = "someName";
    configPropertySource = new ConfigPropertySource(someName, someConfig);
  }

  @Test
  public void testGetPropertyNames() throws Exception {
    String somePropertyName = "somePropertyName";
    String anotherPropertyName = "anotherPropertyName";
    Set<String> somePropertyNames = Sets.newHashSet(somePropertyName, anotherPropertyName);

    when(someConfig.getPropertyNames()).thenReturn(somePropertyNames);

    String[] result = configPropertySource.getPropertyNames();

    verify(someConfig, times(1)).getPropertyNames();

    assertEquals(somePropertyNames, Sets.newHashSet(result));
  }

  @Test
  public void testGetEmptyPropertyNames() throws Exception {
    when(someConfig.getPropertyNames()).thenReturn(Sets.<String>newHashSet());

    assertEquals(0, configPropertySource.getPropertyNames().length);
  }

  @Test
  public void testGetProperty() throws Exception {
    String somePropertyName = "somePropertyName";

    String someValue = "someValue";

    when(someConfig.getProperty(somePropertyName, null)).thenReturn(someValue);

    assertEquals(someValue, configPropertySource.getProperty(somePropertyName));

    verify(someConfig, times(1)).getProperty(somePropertyName, null);
  }

  @Test
  public void testAddChangeListener() throws Exception {
    DatumChangeListener someListener = mock(DatumChangeListener.class);
    DatumChangeListener anotherListener = mock(DatumChangeListener.class);

    final List<DatumChangeListener> listeners = Lists.newArrayList();

    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        listeners.add(invocation.getArgument(0, DatumChangeListener.class));

        return Void.class;
      }
    }).when(someConfig).addChangeListener(any(DatumChangeListener.class));

    configPropertySource.addChangeListener(someListener);
    configPropertySource.addChangeListener(anotherListener);

    assertEquals(2, listeners.size());
    assertTrue(listeners.containsAll(Lists.newArrayList(someListener, anotherListener)));
  }
}
