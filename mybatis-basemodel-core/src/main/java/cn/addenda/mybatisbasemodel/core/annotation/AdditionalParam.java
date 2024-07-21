package cn.addenda.mybatisbasemodel.core.annotation;

import cn.addenda.mybatisbasemodel.core.BaseModelELEvaluator;
import org.apache.ibatis.annotations.Param;

import java.lang.annotation.*;

/**
 * @author addenda
 * @since 2023/6/4 23:19
 */
@Inherited
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(AdditionalParamContainer.class)
public @interface AdditionalParam {

  /**
   * 等同于 {@link Param}
   */
  String name();

  /**
   * 使用{@link BaseModelELEvaluator#evaluate(String, Object)}解析表达式获得值
   */
  String[] expression();

  /**
   * 若此参数配为true，使用前会调用{@link BaseModelELEvaluator#evaluate(String, Object)}解析一下
   */
  boolean valuePreEvaluate() default true;

}
