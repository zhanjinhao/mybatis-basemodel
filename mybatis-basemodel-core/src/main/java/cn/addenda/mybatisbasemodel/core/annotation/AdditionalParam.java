package cn.addenda.mybatisbasemodel.core.annotation;

import cn.addenda.mybatisbasemodel.core.AdditionalParamAttr;
import cn.addenda.mybatisbasemodel.core.BaseModelELEvaluator;
import cn.addenda.mybatisbasemodel.core.util.JSqlParserUtils;
import jdk.nashorn.internal.objects.annotations.Setter;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.type.JdbcType;

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
   * 列名
   */
  String columnName() default AdditionalParamAttr.BASE_MODEL_COLUMN;

  /**
   * {@link AdditionalParam#ifValue()} 为true时，会使用{@link BaseModelELEvaluator#evaluate(String, Object)}解析表达式获得值。
   * 否则，会使用{@link JSqlParserUtils#parseExpression(String)}解析表达式获得{@link net.sf.jsqlparser.expression.Expression}
   */
  String expression();

  /**
   * {@link AdditionalParam#expression()} 是不是数值
   */
  boolean ifValue();

  /**
   * 注入的参数的类型
   * airport4Code airport4_code airport_4code
   */
  JdbcType jdbcType();

  /**
   * 是否注入
   */
  boolean ifInjected() default true;

}
