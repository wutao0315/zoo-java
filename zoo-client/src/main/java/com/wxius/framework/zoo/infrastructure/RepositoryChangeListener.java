package com.wxius.framework.zoo.infrastructure;

import java.util.Properties;

public interface RepositoryChangeListener {
  /**
   * Invoked when config repository changes.
   * @param namespace the namespace of this repository change
   * @param newProperties the properties after change
   */
  void onRepositoryChange(String namespace, Properties newProperties) throws Exception;
}
