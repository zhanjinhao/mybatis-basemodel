package cn.addenda.mybatisbasemodel.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class BaseModelContextTest {

  @BeforeEach
  void setUp() {
    // 清理 ThreadLocal 状态
    BaseModelContext.removeFillMode();
    BaseModelContext.removeAble();
    BaseModelContext.setDefaultFillMode(BaseModelContext.FILL_MODE_FORCE);
  }

  @Test
  void testConstants() {
    assertEquals(1, BaseModelContext.FILL_MODE_FORCE);
    assertEquals(2, BaseModelContext.FILL_MODE_NULL);
    assertEquals(3, BaseModelContext.FILL_MODE_EMPTY);
    assertEquals(4, BaseModelContext.FILL_MODE_SKIP);
  }

  @Test
  void testSetAndGetFillMode() {
    BaseModelContext.pushFillMode(BaseModelContext.FILL_MODE_NULL);
    assertEquals(BaseModelContext.FILL_MODE_NULL, BaseModelContext.peekFillMode());
  }

  @Test
  void testMultipleSetFillMode() {
    BaseModelContext.pushFillMode(BaseModelContext.FILL_MODE_FORCE);
    BaseModelContext.pushFillMode(BaseModelContext.FILL_MODE_NULL);
    assertEquals(BaseModelContext.FILL_MODE_NULL, BaseModelContext.peekFillMode());
    BaseModelContext.popFillMode();
    assertEquals(BaseModelContext.FILL_MODE_FORCE, BaseModelContext.peekFillMode());
    BaseModelContext.popFillMode();
    assertEquals(BaseModelContext.FILL_MODE_FORCE, BaseModelContext.peekFillMode());
  }

  @Test
  void testRemoveFillModeWhenEmpty() {
    assertEquals(BaseModelContext.FILL_MODE_FORCE, BaseModelContext.peekFillMode());
    BaseModelContext.popFillMode(); // 不应报错
    assertEquals(BaseModelContext.FILL_MODE_FORCE, BaseModelContext.peekFillMode());
  }

  @Test
  void testGetFillModeDefault() {
    assertEquals(BaseModelContext.FILL_MODE_FORCE, BaseModelContext.peekFillMode());
  }

  @Test
  void testSetDefaultFillMode() {
    BaseModelContext.setDefaultFillMode(BaseModelContext.FILL_MODE_NULL);
    assertEquals(BaseModelContext.FILL_MODE_NULL, BaseModelContext.peekFillMode());
  }

  @Test
  void testRunWithFillMode() {
    AtomicBoolean executed = new AtomicBoolean(false);
    BaseModelContext.runWithFillMode(BaseModelContext.FILL_MODE_NULL, () -> {
      assertEquals(BaseModelContext.FILL_MODE_NULL, BaseModelContext.peekFillMode());
      executed.set(true);
    });
    assertTrue(executed.get());
    assertEquals(BaseModelContext.FILL_MODE_FORCE, BaseModelContext.peekFillMode()); // 恢复默认
  }

  @Test
  void testRunWithFillModeException() {
    assertThrows(RuntimeException.class, () -> {
      BaseModelContext.runWithFillMode(BaseModelContext.FILL_MODE_NULL, () -> {
        throw new RuntimeException("test");
      });
    });
    assertEquals(BaseModelContext.FILL_MODE_FORCE, BaseModelContext.peekFillMode());
  }

  @Test
  void testGetWithFillMode() {
    String result = BaseModelContext.getWithFillMode(BaseModelContext.FILL_MODE_EMPTY, () -> "success");
    assertEquals("success", result);
    assertEquals(BaseModelContext.FILL_MODE_FORCE, BaseModelContext.peekFillMode());
  }

  @Test
  void testValidIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> BaseModelContext.valid((short) 0));
    assertThrows(IllegalArgumentException.class, () -> BaseModelContext.valid((short) 5));
  }

  @Test
  void testEnableAndDisable() {
    BaseModelContext.enable();
    assertTrue(BaseModelContext.ifEnable());

    BaseModelContext.disable();
    assertFalse(BaseModelContext.ifEnable());

    BaseModelContext.popAble();
    assertTrue(BaseModelContext.ifEnable());
  }

  @Test
  void testRunWithDisable() {
    AtomicBoolean executed = new AtomicBoolean(false);
    BaseModelContext.runWithDisable(() -> {
      assertFalse(BaseModelContext.ifEnable());
      executed.set(true);
    });
    assertTrue(executed.get());
    assertTrue(BaseModelContext.ifEnable());
  }

  @Test
  void testGetWithDisable() {
    String result = BaseModelContext.getWithDisable(() -> "disabled");
    assertEquals("disabled", result);
    assertTrue(BaseModelContext.ifEnable());
  }

}
