package com.wxius.framework.zoo.infrastructure;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wxius.framework.zoo.Constants;
import com.wxius.framework.zoo.build.ZooInjector;
import com.wxius.framework.zoo.core.utils.DeferredLoggerFactory;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.wxius.framework.zoo.util.AESCipher;
import com.wxius.framework.zoo.util.ConfigUtil;
import org.slf4j.Logger;

import com.wxius.framework.zoo.core.utils.ClassLoaderUtil;
import com.wxius.framework.zoo.enums.PropertyChangeType;
import com.wxius.framework.zoo.model.ConfigChange;
import com.wxius.framework.zoo.tracer.Tracer;
import com.wxius.framework.zoo.util.ExceptionUtil;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.RateLimiter;

public class DefaultDatum extends AbstractDatum implements RepositoryChangeListener {

  private static final Logger logger = DeferredLoggerFactory.getLogger(DefaultDatum.class);
  private static final Gson GSON = new Gson();
  private final String m_name;
  private final AtomicReference<Properties> m_configProperties;
  private final LocalRepository m_localRepository;
  private final RateLimiter m_warnLogRateLimiter;
  private final ConfigUtil m_configUtil;
  private final AESCipher m_aesCipher;
  /**
   * Constructor.
   *
   * @param name        the namespace of this config instance
   * @param localRepository the config repository for this config instance
   */
  public DefaultDatum(String name, LocalRepository localRepository) {
    m_name = name;
    m_configProperties = new AtomicReference<>();
    m_configProperties.set(loadFromResource(m_name));
    m_localRepository = localRepository;
    m_warnLogRateLimiter = RateLimiter.create(0.017); // 1 warning log output per minute
    m_configUtil = ZooInjector.getInstance(ConfigUtil.class);
    m_aesCipher = ZooInjector.getInstance(AESCipher.class);
    initialize();
  }

  private void initialize() {
    try {
      updateConfig(m_localRepository.getDatum());
    } catch (Throwable ex) {
      Tracer.logError(ex);
      logger.warn("Init Zoo Local Config failed - namespace: {}, reason: {}.",
              m_name, ExceptionUtil.getDetailMessage(ex));
    } finally {
      //register the change listener no matter config repository is working or not
      //so that whenever config repository is recovered, config could get changed
      m_localRepository.addChangeListener(this);
    }
  }

  /**
   * get property from cached repository properties file
   *
   * @param key property key
   * @return value
   */
  protected String getPropertyFromRepository(String key) {
    Properties properties = m_configProperties.get();
    if (properties != null) {
      return properties.getProperty(key);
    }
    return null;
  }

  /**
   * get property from additional properties file on classpath
   *
   * @param key property key
   * @return value
   */
  protected String getPropertyFromAdditional(String key) {
    Properties properties = this.m_configProperties.get();
    if (properties != null) {
      return properties.getProperty(key);
    }
    return null;
  }

  /**
   * try to print a warn log when can not find a property
   *
   * @param value value
   */
  protected void tryWarnLog(String value) {
    if (value == null && m_configProperties.get() == null && m_warnLogRateLimiter.tryAcquire()) {
      logger.warn(
          "Could not load config for namespace {} from Zoo, please check whether the configs are released in Zoo! Return default value now!",
              m_name);
    }
  }

  /**
   * get property names from cached repository properties file
   *
   * @return property names
   */
  protected Set<String> getPropertyNamesFromRepository() {
    Properties properties = m_configProperties.get();
    if (properties == null) {
      return Collections.emptySet();
    }
    return this.stringPropertyNames(properties);
  }

  /**
   * get property names from additional properties file on classpath
   *
   * @return property names
   */
  protected Set<String> getPropertyNamesFromAdditional() {
    Properties properties = this.m_configProperties.get();
    if (properties == null) {
      return Collections.emptySet();
    }
    return this.stringPropertyNames(properties);
  }

  @Override
  public String getProperty(String key, String defaultValue) {
    // step 1: check system properties, i.e. -Dkey=value
    String value = System.getProperty(key);

    // step 2: check local cached properties file
    if (value == null) {
      value = this.getPropertyFromRepository(key);
    }

    /*
     * step 3: check env variable, i.e. PATH=...
     * normally system environment variables are in UPPERCASE, however there might be exceptions.
     * so the caller should provide the key in the right case
     */
    if (value == null) {
      value = System.getenv(key);
    }

    // step 4: check properties file from classpath
    if (value == null) {
      value = this.getPropertyFromAdditional(key);
    }

    this.tryWarnLog(value);

    return value == null ? defaultValue : value;
  }

  @Override
  public Set<String> getPropertyNames() {
    // propertyNames include system property and system env might cause some compatibility issues, though that looks like the correct implementation.
    Set<String> fromRepository = this.getPropertyNamesFromRepository();
    Set<String> fromAdditional = this.getPropertyNamesFromAdditional();
    if (fromRepository == null || fromRepository.isEmpty()) {
      return fromAdditional;
    }
    if (fromAdditional == null || fromAdditional.isEmpty()) {
      return fromRepository;
    }
    Set<String> propertyNames = Sets
        .newLinkedHashSetWithExpectedSize(fromRepository.size() + fromAdditional.size());
    propertyNames.addAll(fromRepository);
    propertyNames.addAll(fromAdditional);
    return propertyNames;
  }

  private Set<String> stringPropertyNames(Properties properties) {
    //jdk9以下版本Properties#enumerateStringProperties方法存在性能问题，keys() + get(k) 重复迭代, jdk9之后改为entrySet遍历.
    Map<String, String> h = Maps.newLinkedHashMapWithExpectedSize(properties.size());
    for (Map.Entry<Object, Object> e : properties.entrySet()) {
      Object k = e.getKey();
      Object v = e.getValue();
      if (k instanceof String && v instanceof String) {
        h.put((String) k, (String) v);
      }
    }
    return h.keySet();
  }

  @Override
  public synchronized void onRepositoryChange(String datumName, Properties newProperties)
  {

    String value = newProperties.getProperty(Constants.Encrypt);
    if (m_configUtil.isEncryptEnable() && value != null && !value.isEmpty())
    {
      try {
        String decryptVal = m_aesCipher.decrypt(value);
        Map<String,String> decryptMap = GSON.fromJson(decryptVal, TypeToken.getParameterized(Map.class, String.class, String.class).getType());
        Properties decryptProperties = propertiesFactory.getPropertiesInstance();
        decryptProperties.putAll(decryptMap);
        newProperties = decryptProperties;
      }
      catch (Throwable ex)
      {
        throw new RuntimeException(ex);
      }
    }

    if (newProperties.equals(m_configProperties.get())) {
      return;
    }

    Properties newConfigProperties = propertiesFactory.getPropertiesInstance();
    newConfigProperties.putAll(newProperties);

    Map<String, ConfigChange> actualChanges = updateAndCalcConfigChanges(newConfigProperties);

    // check double checked result
    if (actualChanges.isEmpty()) {
      return;
    }

    this.fireConfigChange(m_name, actualChanges);

    Tracer.logEvent("Zoo.Client.ConfigChanges", m_name);
  }

  private void updateConfig(Properties newConfigProperties) {
    m_configProperties.set(newConfigProperties);
  }

  private Map<String, ConfigChange> updateAndCalcConfigChanges(Properties newConfigProperties) {
    List<ConfigChange> configChanges =
        calcPropertyChanges(m_name, m_configProperties.get(), newConfigProperties);

    ImmutableMap.Builder<String, ConfigChange> actualChanges =
        new ImmutableMap.Builder<>();

    /** === Double check since DefaultConfig has multiple config sources ==== **/

    //1. use getProperty to update configChanges's old value
    for (ConfigChange change : configChanges) {
      change.setOldValue(this.getProperty(change.getPropertyName(), change.getOldValue()));
    }

    //2. update m_configProperties
    updateConfig(newConfigProperties);
    clearConfigCache();

    //3. use getProperty to update configChange's new value and calc the final changes
    for (ConfigChange change : configChanges) {
      change.setNewValue(this.getProperty(change.getPropertyName(), change.getNewValue()));
      switch (change.getChangeType()) {
        case ADDED:
          if (Objects.equals(change.getOldValue(), change.getNewValue())) {
            break;
          }
          if (change.getOldValue() != null) {
            change.setChangeType(PropertyChangeType.MODIFIED);
          }
          actualChanges.put(change.getPropertyName(), change);
          break;
        case MODIFIED:
          if (!Objects.equals(change.getOldValue(), change.getNewValue())) {
            actualChanges.put(change.getPropertyName(), change);
          }
          break;
        case DELETED:
          if (Objects.equals(change.getOldValue(), change.getNewValue())) {
            break;
          }
          if (change.getNewValue() != null) {
            change.setChangeType(PropertyChangeType.MODIFIED);
          }
          actualChanges.put(change.getPropertyName(), change);
          break;
        default:
          //do nothing
          break;
      }
    }
    return actualChanges.build();
  }

  private Properties loadFromResource(String name) {
    String path = String.format("META-INF/config/%s.properties", name);
    InputStream in = ClassLoaderUtil.getLoader().getResourceAsStream(path);
    Properties properties = null;

    if (in != null) {
      properties = propertiesFactory.getPropertiesInstance();

      try {
        properties.load(in);
      } catch (IOException ex) {
        Tracer.logError(ex);
        logger.error("Load resource config for namespace {} failed", path, ex);
      } finally {
        try {
          in.close();
        } catch (IOException ex) {
          // ignore
        }
      }
    }

    return properties;
  }
}
