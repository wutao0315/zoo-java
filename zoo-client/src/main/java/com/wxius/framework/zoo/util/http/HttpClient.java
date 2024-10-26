
package com.wxius.framework.zoo.util.http;

import com.wxius.framework.zoo.exceptions.ZooException;

import java.lang.reflect.Type;


public interface HttpClient {

  /**
   * Do get operation for the http request.
   *
   * @param httpRequest  the request
   * @param responseType the response type
   * @return the response
   * @throws ZooException if any error happened or response code is neither 200 nor 304
   */
  <T> HttpResponse<T> doGet(HttpRequest httpRequest, final Class<T> responseType)
      throws ZooException;

  /**
   * Do get operation for the http request.
   *
   * @param httpRequest  the request
   * @param responseType the response type
   * @return the response
   * @throws ZooException if any error happened or response code is neither 200 nor 304
   */
  <T> HttpResponse<T> doGet(HttpRequest httpRequest, final Type responseType)
      throws ZooException;
}
