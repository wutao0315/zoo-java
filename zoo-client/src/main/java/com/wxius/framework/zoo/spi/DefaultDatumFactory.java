package com.wxius.framework.zoo.spi;

import com.google.common.collect.Maps;
import com.wxius.framework.zoo.Datum;
//import com.wxius.framework.zoo.ConfigFile;
import com.wxius.framework.zoo.ConfigService;
//import com.wxius.framework.zoo.PropertiesCompatibleConfigFile;
import com.wxius.framework.zoo.build.ZooInjector;
//import com.wxius.framework.zoo.core.enums.ConfigFileFormat;
import com.wxius.framework.zoo.infrastructure.DatumRepository;
import com.wxius.framework.zoo.infrastructure.DefaultDatum;
//import com.wxius.framework.zoo.infrastructure.JsonConfigFile;
import com.wxius.framework.zoo.infrastructure.LocalRepository;
//import com.wxius.framework.zoo.infrastructure.PropertiesCompatibleFileConfigRepository;
//import com.wxius.framework.zoo.infrastructure.PropertiesConfigFile;
import com.wxius.framework.zoo.infrastructure.HttpRepository;
//import com.wxius.framework.zoo.infrastructure.TxtConfigFile;
//import com.wxius.framework.zoo.infrastructure.XmlConfigFile;
//import com.wxius.framework.zoo.infrastructure.YamlConfigFile;
//import com.wxius.framework.zoo.infrastructure.YmlConfigFile;
import com.wxius.framework.zoo.util.AESCipher;
import com.wxius.framework.zoo.util.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * The default implementation of {@link DatumFactory}.
 * <p>
 * Supports namespaces of format:
 *
 */
public class DefaultDatumFactory implements DatumFactory {

  private static final Logger logger = LoggerFactory.getLogger(DefaultDatumFactory.class);
  private final Map<String, LocalRepository> _repositories = Maps.newConcurrentMap();
  public DefaultDatumFactory()
  {
  }

  @Override
  public Datum create(String name) {

    LocalRepository repository = getRepository(name);

    DefaultDatum datum = new DefaultDatum(name, repository);

    logger.debug("Created a configuration repository of type [{}] for name [{}]",
            repository.getClass().getName(), name);

    return datum;
  }

  LocalRepository getRepository(String name) {
    LocalRepository local = _repositories.get(name);
    if(local == null)
    {
      local = createRepository(name);
      _repositories.put(name, local);
    }
    return local;
  }

  /**
   * Creates a local repository for a given namespace
   *
   * @param name the namespace of the repository
   * @return the newly created repository for the given namespace
   */
  protected LocalRepository createRepository(String name) {
    return new LocalRepository(name, new HttpRepository(name));
  }
}
