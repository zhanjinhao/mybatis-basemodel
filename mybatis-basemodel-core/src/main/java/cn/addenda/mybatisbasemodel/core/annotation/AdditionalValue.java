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
   * {@link AdditionalValue#ifObj()} 为true时，会使用{@link BaseModelELEvaluator#evaluate(String, Object)}解析表达式获得值。
   * 否则，会使用{@link JSqlParserUtils#parseExpression(String)}解析表达式获得{@link net.sf.jsqlparser.expression.Expression}
   */
  String expression();

  /**
   * {@link AdditionalValue#expression()} 是不是值
   */
  boolean ifObj();

  /**
   * 注入的参数的类型
   */
  JdbcType jdbcType();

  /**
   * 当 {@link AdditionalValue#ifObj()} 为false时。
   * 若此参数配为true，调用{@link JSqlParserUtils#parseExpression(String)}前调用{@link BaseModelELEvaluator#evaluate(String, Object)}解析一下
   */
  boolean expressionPreEvaluate() default false;

  /**
   * 当 {@link AdditionalValue#ifObj()} 为true时。
   * 若此参数配为true，使用前调用{@link BaseModelELEvaluator#evaluate(String, Object)}解析一下
   */
  boolean objPreEvaluate() default true;

}
