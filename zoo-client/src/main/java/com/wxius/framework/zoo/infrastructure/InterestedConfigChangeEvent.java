package com.wxius.framework.zoo.infrastructure;

import com.wxius.framework.zoo.Datum;
import com.wxius.framework.zoo.DatumChangeListener;
import com.wxius.framework.zoo.model.ConfigChange;
import com.wxius.framework.zoo.model.ConfigChangeEvent;
import com.wxius.framework.zoo.spring.annotation.ZooConfigChangeListener;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * In {@link ZooConfigChangeListener} you set some interested key's rule, you can get those keys
 * by this class's instance.
 *
 */
class InterestedConfigChangeEvent extends ConfigChangeEvent {

  /**
   * @see Datum#addChangeListener(DatumChangeListener, Set)
   * @see Datum#addChangeListener(DatumChangeListener, Set, Set)
   * @see ZooConfigChangeListener#interestedKeys()
   * @see ZooConfigChangeListener#interestedKeyPrefixes()
   */
  private final Set<String> m_interestedChangedKeys;

  public InterestedConfigChangeEvent(String name,
      Map<String, ConfigChange> changes, Set<String> interestedChangedKeys) {
    super(name, changes);
    this.m_interestedChangedKeys = interestedChangedKeys;
  }

  /**
   * @return interested and changed keys
   */
  @Override
  public Set<String> interestedChangedKeys() {
    return Collections.unmodifiableSet(this.m_interestedChangedKeys);
  }
}
