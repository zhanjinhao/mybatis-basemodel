package cn.addenda.mybatisbasemodel.simple;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectionException;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestMetaObject {

  @Test
  void test1() {
    MetaObject metaObject = SystemMetaObject.forObject("1");
    Assertions.assertThrows(ReflectionException.class, () -> {
      metaObject.getValue("a");
    }, "There is no getter for property named 'a' in 'class java.lang.String'");
  }

}
