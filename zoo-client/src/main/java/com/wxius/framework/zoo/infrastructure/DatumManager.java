package com.wxius.framework.zoo.infrastructure;

import com.wxius.framework.zoo.Datum;
//import com.wxius.framework.zoo.ConfigFile;
//import com.wxius.framework.zoo.core.enums.ConfigFileFormat;

public interface DatumManager {
  /**
   * Get the config instance for the namespace specified.
   * @param name the namespace
   * @return the config instance for the namespace
   */
  Datum getDatum(String name);
}
