package cn.addenda.mybatisbasemodel.core;

public class BaseModelException extends RuntimeException {

  public BaseModelException() {
    super();
  }

  public BaseModelException(String message) {
    super(message);
  }

  public BaseModelException(String message, Throwable cause) {
    super(message, cause);
  }

  public BaseModelException(Throwable cause) {
    super(cause);
  }

  protected BaseModelException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
