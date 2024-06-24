package cn.addenda.mybatisbasemodel.simple;

import cn.addenda.mybatisbasemodel.spring.SpELUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SimpleBaseModelSourceTest {

  @Test
  void test1() {
    SimpleBaseModelSource.setUser("123");
    try {
      Assertions.assertEquals("123", SpELUtils.getObject(SimpleBaseModelSource.USER_EL, null));
    } finally {
      SimpleBaseModelSource.removeUser();
    }
  }

}
