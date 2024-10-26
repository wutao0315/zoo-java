package com.wxius.framework.zoo.infrastructure;

import java.util.Properties;

public interface DatumRepository {
  /**
   * Get the config from this repository.
   * @return config
   */
  Properties getDatum();

  /**
   * Set the fallback repo for this repository.
   * @param upstreamConfigRepository the upstream repo
   */
  void setUpstreamRepository(DatumRepository upstreamConfigRepository);

  /**
   * Add change listener.
   * @param listener the listener to observe the changes
   */
  void addChangeListener(RepositoryChangeListener listener);

  /**
   * Remove change listener.
   * @param listener the listener to remove
   */
  void removeChangeListener(RepositoryChangeListener listener);

//  /**
//   * Return the config's source type, i.e. where is the config loaded from
//   *
//   * @return the config's source type
//   */
//  ConfigSourceType getSourceType();
}
