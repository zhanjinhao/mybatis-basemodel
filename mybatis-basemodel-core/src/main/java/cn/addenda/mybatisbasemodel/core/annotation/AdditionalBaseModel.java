package cn.addenda.mybatisbasemodel.core.annotation;

import cn.addenda.mybatisbasemodel.core.BaseModel;

import java.lang.annotation.*;

/**
 * @author addenda
 * @since 2023/6/4 23:19
 */
@Inherited
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AdditionalBaseModel {

  Class<? extends BaseModel> value();

}
