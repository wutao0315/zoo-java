package com.wxius.framework.zoo;

import com.wxius.framework.zoo.model.ConfigChangeEvent;

public interface DatumChangeListener {
  /**
   * Invoked when there is any config change for the namespace.
   * @param changeEvent the event for this change
   */
  void onChange(ConfigChangeEvent changeEvent);
}
