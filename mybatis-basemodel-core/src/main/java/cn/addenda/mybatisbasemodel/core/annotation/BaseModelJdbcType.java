package cn.addenda.mybatisbasemodel.core.annotation;

import org.apache.ibatis.type.JdbcType;

import java.lang.annotation.*;

/**
 * @author addenda
 * @since 2023/6/4 23:19
 */
@Inherited
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BaseModelJdbcType {

  JdbcType value();

}
