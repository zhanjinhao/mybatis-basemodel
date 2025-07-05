package cn.addenda.mybatisbasemodel.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Stack;
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

  private static final ThreadLocal<Stack<Short>> FILL_MODE_TL = ThreadLocal.withInitial(() -> null);

  public static void removeFillMode() {
    FILL_MODE_TL.remove();
  }

  public static void pushFillMode(short s) {
    valid(s);
    Stack<Short> modes = FILL_MODE_TL.get();
    if (modes == null) {
      modes = new Stack<>();
      FILL_MODE_TL.set(modes);
    }
    modes.push(s);
  }

  public static void popFillMode() {
    Stack<Short> modes = FILL_MODE_TL.get();
    if (modes == null) {
      return;
    }
    modes.pop();
    if (modes.isEmpty()) {
      FILL_MODE_TL.remove();
    }
  }

  public static short peekFillMode() {
    Stack<Short> shorts = FILL_MODE_TL.get();
    if (shorts == null) {
      return defaultFillMode;
    }
    return shorts.peek();
  }

  public static void setDefaultFillMode(short s) {
    valid(s);
    defaultFillMode = s;
  }

  static void valid(short s) {
    if (s != FILL_MODE_FORCE && s != FILL_MODE_NULL && s != FILL_MODE_EMPTY && s != FILL_MODE_SKIP) {
      throw new IllegalArgumentException(String.valueOf(s));
    }
  }

  public static void runWithFillMode(short fillMode, Runnable runnable) {
    pushFillMode(fillMode);
    try {
      runnable.run();
    } finally {
      popFillMode();
    }
  }

  public static <T> T getWithFillMode(short fillMode, Supplier<T> supplier) {
    pushFillMode(fillMode);
    try {
      return supplier.get();
    } finally {
      popFillMode();
    }
  }

  private static final ThreadLocal<Stack<Boolean>> ENABLE_TL = java.lang.ThreadLocal.withInitial(() -> null);

  public static void removeAble() {
    ENABLE_TL.remove();
  }

  public static void disable() {
    Stack<Boolean> ables = ENABLE_TL.get();
    if (ables == null) {
      ables = new Stack<>();
      ENABLE_TL.set(ables);
    }
    ables.push(false);
  }

  public static void enable() {
    Stack<Boolean> ables = ENABLE_TL.get();
    if (ables == null) {
      ables = new Stack<>();
      ENABLE_TL.set(ables);
    }
    ables.push(true);
  }

  public static void popAble() {
    Stack<Boolean> ables = ENABLE_TL.get();
    if (ables == null) {
      return;
    }
    ables.pop();
    if (ables.isEmpty()) {
      ENABLE_TL.remove();
    }
  }

  public static boolean ifEnable() {
    Stack<Boolean> booleans = ENABLE_TL.get();
    if (booleans == null) {
      return true;
    }
    return booleans.peek();
  }

  public static void runWithDisable(Runnable runnable) {
    disable();
    try {
      runnable.run();
    } finally {
      popAble();
    }
  }

  public static <T> T getWithDisable(Supplier<T> supplier) {
    disable();
    try {
      return supplier.get();
    } finally {
      popAble();
    }
  }

}
