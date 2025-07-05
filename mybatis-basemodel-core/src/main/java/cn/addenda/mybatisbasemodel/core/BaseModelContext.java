package cn.addenda.mybatisbasemodel.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BaseModelContext {

  /**
   * 无论BaseModel的字段有没有数据都执行填充
   */
  public static final short FILL_MODE_FORCE = 1;

  /**
   * 只有值为null的字段才执行填充
   */
  public static final short FILL_MODE_NULL = 2;

  /**
   * 只有值为empty的字段才执行填充
   */
  public static final short FILL_MODE_EMPTY = 3;

  /**
   * 所有字段均不更新
   */
  public static final short FILL_MODE_SKIP = 4;

  /**
   * 默认填充模式，可以修改
   */
  private static short defaultFillMode = FILL_MODE_FORCE;

  private static final ThreadLocal<Short> FILL_MODE_TL = ThreadLocal.withInitial(() -> defaultFillMode);

  public static void setFillMode(short s) {
    if (s != FILL_MODE_FORCE && s != FILL_MODE_NULL && s != FILL_MODE_EMPTY && s != FILL_MODE_SKIP) {
      throw new IllegalArgumentException(String.valueOf(s));
    }
    FILL_MODE_TL.set(s);
  }

  public static void resetFillMode() {
    FILL_MODE_TL.set(defaultFillMode);
  }

  public static short getFillMode() {
    return FILL_MODE_TL.get();
  }

  public static void setDefaultFillMode(short s) {
    if (s != FILL_MODE_FORCE && s != FILL_MODE_NULL && s != FILL_MODE_EMPTY && s != FILL_MODE_SKIP) {
      throw new IllegalArgumentException(String.valueOf(s));
    }
    defaultFillMode = s;
  }

  public static void runWithFillMode(short fillMode, Runnable runnable) {
    setFillMode(fillMode);
    try {
      runnable.run();
    } finally {
      resetFillMode();
    }
  }

  public static <T> T getWithFillMode(short fillMode, Supplier<T> supplier) {
    setFillMode(fillMode);
    try {
      return supplier.get();
    } finally {
      resetFillMode();
    }
  }

  private static final ThreadLocal<Boolean> ENABLE_TL = java.lang.ThreadLocal.withInitial(() -> true);

  public static void disable() {
    ENABLE_TL.set(false);
  }

  public static void enable() {
    ENABLE_TL.set(true);
  }

  public static boolean ifEnable() {
    return ENABLE_TL.get();
  }

  public static void runWithDisable(Runnable runnable) {
    disable();
    try {
      runnable.run();
    } finally {
      enable();
    }
  }

  public static <T> T getWithDisable(Supplier<T> supplier) {
    disable();
    try {
      return supplier.get();
    } finally {
      enable();
    }
  }

}
