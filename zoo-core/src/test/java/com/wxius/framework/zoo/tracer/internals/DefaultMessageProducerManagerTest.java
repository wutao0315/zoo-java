
package com.wxius.framework.zoo.tracer.internals;

import com.wxius.framework.zoo.tracer.internals.cat.CatMessageProducer;
import com.wxius.framework.zoo.tracer.spi.MessageProducerManager;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class DefaultMessageProducerManagerTest {
  private MessageProducerManager messageProducerManager;

  @Before
  public void setUp() throws Exception {
    messageProducerManager = new DefaultMessageProducerManager();
  }

  @Test
  public void testGetProducer() throws Exception {
    assertTrue(messageProducerManager.getProducer() instanceof CatMessageProducer);
  }

}