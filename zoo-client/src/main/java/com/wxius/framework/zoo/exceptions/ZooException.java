package com.wxius.framework.zoo.exceptions;

public class ZooException extends RuntimeException {
  public ZooException(String message) {
    super(message);
  }

  public ZooException(String message, Throwable cause) {
    super(message, cause);
  }
}
