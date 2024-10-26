package com.wxius.framework.zoo.exceptions;

public class ZooStatusCodeException extends RuntimeException{
  private final int m_statusCode;

  public ZooStatusCodeException(int statusCode, String message) {
    super(String.format("[status code: %d] %s", statusCode, message));
    this.m_statusCode = statusCode;
  }

  public ZooStatusCodeException(int statusCode, Throwable cause) {
    super(cause);
    this.m_statusCode = statusCode;
  }

  public int getStatusCode() {
    return m_statusCode;
  }
}
