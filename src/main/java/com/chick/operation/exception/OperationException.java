package com.chick.operation.exception;

import lombok.Data;

@Data
public class OperationException extends RuntimeException {

  private String code;

  public OperationException() {
    super();
  }

  public OperationException(String code, String message) {
    super(message);
    this.code = code;
  }

  public OperationException(String message) {
    super(message);
  }

  public OperationException(String message, Throwable cause) {
    super(message, cause);
  }

  public OperationException(Throwable cause) {
    super(cause);
  }
}
