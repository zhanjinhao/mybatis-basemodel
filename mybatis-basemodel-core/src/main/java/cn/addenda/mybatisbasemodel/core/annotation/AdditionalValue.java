package cn.addenda.mybatisbasemodel.core.annotation;

import cn.addenda.mybatisbasemodel.core.AdditionAttr;
import cn.addenda.mybatisbasemodel.core.BaseModelELEvaluator;
import cn.addenda.mybatisbasemodel.core.util.JSqlParserUtils;
import org.apache.ibatis.type.JdbcType;

import java.lang.annotation.*;

/**
 * @author addenda
 * @since 2023/6/4 23:19
 */
@Inherited
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(AdditionalValueContainer.class)
public @interface AdditionalValue {

  /**
   * 字段名。
   */
  String name();

  /**
   * 列名
   */
  String columnName() default AdditionAttr.BASE_MODEL_COLUMN;

  /**
   * {@link AdditionalValue#ifValue()} 为true时，会使用{@link BaseModelELEvaluator#evaluate(String, Object)}解析表达式获得值。
   * 否则，会使用{@link JSqlParserUtils#parseExpression(String)}解析表达式获得{@link net.sf.jsqlparser.expression.Expression}
   */
  String expression();

  /**
   * {@link AdditionalValue#expression()} 是不是数值
   */
  boolean ifValue();

  /**
   * 注入的参数的类型
   */
  JdbcType jdbcType();

}