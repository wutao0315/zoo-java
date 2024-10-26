
package com.wxius.framework.zoo.util;

import com.google.common.collect.Maps;
import com.google.common.io.BaseEncoding;
import com.google.common.net.HttpHeaders;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;


public class Signature {

  /**
   * Authorization=Zoo {appId}:{sign}
   */
  private static final String AUTHORIZATION_FORMAT = "%s:%s:%s";
  private static final String DELIMITER = "|";

  private static final String ENCODING = "UTF-8";
  public static String genSign(String appId, String secret) throws UnsupportedEncodingException {
    long currentTimeMillis = System.currentTimeMillis();
    String timestamp = String.valueOf(currentTimeMillis);
    String data = timestamp+DELIMITER+appId+DELIMITER+secret;
    String sign = HmacSha1Utils.signString(data, secret);
    String token = String.format(AUTHORIZATION_FORMAT, appId, timestamp, sign);
    byte[] tokenBytes = token.getBytes(ENCODING);
    String tokenBase64 = BaseEncoding.base64().encode(tokenBytes);
    return tokenBase64;
  }

  public static String buildHttpHeaders(String appId, String secret) throws UnsupportedEncodingException {
    String tokenBase64 = genSign(appId, secret);
    return "api-zoo "+ tokenBase64;
  }
}
