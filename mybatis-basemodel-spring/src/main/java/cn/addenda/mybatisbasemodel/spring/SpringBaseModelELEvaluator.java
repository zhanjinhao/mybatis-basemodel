package cn.addenda.mybatisbasemodel.spring;

import cn.addenda.mybatisbasemodel.core.BaseModelELEvaluator;

public class SpringBaseModelELEvaluator implements BaseModelELEvaluator {
  @Override
  public Object evaluate(String el, Object argument) {
    return SpELUtils.getObjectIgnoreException(el, argument);
  }

}
