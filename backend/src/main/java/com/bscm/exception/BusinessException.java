package com.bscm.exception;

import lombok.Getter;

/** 业务异常类 用于处理业务逻辑中的异常情况 */
@Getter
public class BusinessException extends RuntimeException {

  private final Integer code;
  private final String message;

  public BusinessException(String message) {
    super(message);
    this.code = 400;
    this.message = message;
  }

  public BusinessException(Integer code, String message) {
    super(message);
    this.code = code;
    this.message = message;
  }

  public BusinessException(String message, Throwable cause) {
    super(message, cause);
    this.code = 400;
    this.message = message;
  }
}
