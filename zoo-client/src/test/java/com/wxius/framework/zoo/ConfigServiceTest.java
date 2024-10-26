package com.wxius.framework.zoo;

import static org.junit.Assert.assertEquals;

import com.wxius.framework.zoo.core.MetaDomainConsts;
//import com.wxius.framework.zoo.enums.ConfigSourceType;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wxius.framework.zoo.build.MockInjector;
import com.wxius.framework.zoo.core.ConfigConsts;
//import com.wxius.framework.zoo.core.enums.ConfigFileFormat;
import com.wxius.framework.zoo.infrastructure.AbstractDatum;
import com.wxius.framework.zoo.spi.DatumFactory;
import com.wxius.framework.zoo.util.ConfigUtil;
import org.springframework.test.util.ReflectionTestUtils;

public class ConfigServiceTest {
  private static String someAppId;

  @Before
  public void setUp() throws Exception {
    someAppId = "someAppId";

    MockInjector.setInstance(ConfigUtil.class, new MockConfigUtil());
  }

  @After
  public void tearDown() throws Exception {
    //as ConfigService is singleton, so we must manually clear its container
    ConfigService.reset();
    MockInjector.reset();
    ReflectionTestUtils.invokeMethod(MetaDomainConsts.class, "reset");
  }

  @Test
  public void testHackConfig() {
    String someNamespace = "hack";
    String someKey = "first";
    ConfigService.setConfig(new MockConfig(someNamespace));

    Datum config = ConfigService.getAppConfig();

    assertEquals(someNamespace + ":" + someKey, config.getProperty(someKey, null));
    assertEquals(null, config.getProperty("unknown", null));
  }

  @Test
  public void testHackConfigFactory() throws Exception {
    String someKey = "someKey";
    ConfigService.setConfigFactory(new MockConfigFactory());

    Datum config = ConfigService.getAppConfig();

    assertEquals(ConfigConsts.NAMESPACE_APPLICATION + ":" + someKey,
        config.getProperty(someKey, null));
  }

  @Test
  public void testMockConfigFactory() throws Exception {
    String someNamespace = "mock";
    String someKey = "someKey";
    MockInjector.setInstance(DatumFactory.class, someNamespace, new MockConfigFactory());

    Datum config = ConfigService.getConfig(someNamespace);

    assertEquals(someNamespace + ":" + someKey, config.getProperty(someKey, null));
    assertEquals(null, config.getProperty("unknown", null));
  }

  @Test
  public void testMockConfigFactoryForConfigFile() throws Exception {
    String someNamespace = "mock";
//    ConfigFileFormat someConfigFileFormat = ConfigFileFormat.Properties;
//    String someNamespaceFileName =
//        String.format("%s.%s", someNamespace, someConfigFileFormat.getValue());
//    MockInjector.setInstance(DatumFactory.class, someNamespaceFileName, new MockConfigFactory());
//
//    ConfigFile configFile = ConfigService.getConfigFile(someNamespace, someConfigFileFormat);
//
//    assertEquals(someNamespaceFileName, configFile.getNamespace());
//    assertEquals(someNamespaceFileName + ":" + someConfigFileFormat.getValue(), configFile.getContent());
  }

  private static class MockConfig extends AbstractDatum {
    private final String m_namespace;

    public MockConfig(String namespace) {
      m_namespace = namespace;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
      if (key.equals("unknown")) {
        return null;
      }

      return m_namespace + ":" + key;
    }

    @Override
    public Set<String> getPropertyNames() {
      return null;
    }

//    @Override
//    public ConfigSourceType getSourceType() {
//      return null;
//    }
  }

//  private static class MockConfigFile implements ConfigFile {
//    private ConfigFileFormat m_configFileFormat;
//    private String m_namespace;
//
//    public MockConfigFile(String namespace,
//                          ConfigFileFormat configFileFormat) {
//      m_namespace = namespace;
//      m_configFileFormat = configFileFormat;
//    }
//
//    @Override
//    public String getContent() {
//      return m_namespace + ":" + m_configFileFormat.getValue();
//    }
//
//    @Override
//    public boolean hasContent() {
//      return true;
//    }
//
//    @Override
//    public String getNamespace() {
//      return m_namespace;
//    }
//
//    @Override
//    public ConfigFileFormat getConfigFileFormat() {
//      return m_configFileFormat;
//    }
//
//    @Override
//    public void addChangeListener(ConfigFileChangeListener listener) {
//
//    }
//
//    @Override
//    public boolean removeChangeListener(ConfigFileChangeListener listener) {
//      return false;
//    }
//
//    @Override
//    public ConfigSourceType getSourceType() {
//      return null;
//    }
//  }

  public static class MockConfigFactory implements DatumFactory {
    @Override
    public Datum create(String namespace) {
      return new MockConfig(namespace);
    }

//    @Override
//    public ConfigFile createConfigFile(String namespace, ConfigFileFormat configFileFormat) {
//      return new MockConfigFile(namespace, configFileFormat);
//    }
  }

  public static class MockConfigUtil extends ConfigUtil {
    @Override
    public String getAppId() {
      return someAppId;
    }
  }

}
