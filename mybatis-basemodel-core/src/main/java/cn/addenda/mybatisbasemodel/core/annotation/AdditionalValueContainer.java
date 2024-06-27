package cn.addenda.mybatisbasemodel.core.annotation;

import java.lang.annotation.*;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AdditionalValueContainer {

  AdditionalValue[] value();
}
