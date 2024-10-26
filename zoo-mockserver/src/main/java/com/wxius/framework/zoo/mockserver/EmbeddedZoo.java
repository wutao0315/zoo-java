package com.wxius.framework.zoo.mockserver;

import org.junit.rules.ExternalResource;

/**
 * Create by zhangzheng on 8/22/18 Email:zhangzheng@youzan.com
 */
public class EmbeddedZoo extends ExternalResource {

  private ZooTestingServer zoo = new ZooTestingServer();

  @Override
  protected void before() throws Throwable {
    zoo.start();
    super.before();
  }

  @Override
  protected void after() {
    zoo.close();
  }

  /**
   * Add new property or update existed property
   */
  public void addOrModifyProperty(String namespace, String someKey, String someValue) {
    zoo.addOrModifyProperty(namespace, someKey, someValue);
  }

  /**
   * Delete existed property
   */
  public void deleteProperty(String namespace, String someKey) {
    zoo.deleteProperty(namespace, someKey);
  }

  /**
   * reset overridden properties
   */
  public void resetOverriddenProperties() {
    zoo.resetOverriddenProperties();
  }
}
