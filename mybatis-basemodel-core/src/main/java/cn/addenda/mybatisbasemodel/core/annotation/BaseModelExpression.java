package cn.addenda.mybatisbasemodel.core.annotation;

import java.lang.annotation.*;

/**
 * @author addenda
 * @since 2023/6/4 23:19
 */
@Inherited
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BaseModelExpression {

  String expression();

  boolean ifObj();

}
