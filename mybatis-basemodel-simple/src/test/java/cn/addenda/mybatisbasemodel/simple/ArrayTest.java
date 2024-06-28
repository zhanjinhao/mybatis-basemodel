package cn.addenda.mybatisbasemodel.simple;

import cn.addenda.mybatisbasemodel.core.annotation.AdditionalValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class ArrayTest {

  @Test
  void test1() {
    AdditionalValue[] additionalValues1 = new AdditionalValue[0];
    List<AdditionalValue> collect1 = Arrays.stream(additionalValues1).collect(Collectors.toList());
    Assertions.assertEquals(0, collect1.size());
    AdditionalValue[] additionalValues2 = new AdditionalValue[0];
    List<AdditionalValue> collect2 = Arrays.stream(additionalValues2).collect(Collectors.toList());
    Assertions.assertEquals(0, collect2.size());

    collect1.addAll(collect2);
    Assertions.assertEquals(0, collect2.size());
  }

}
