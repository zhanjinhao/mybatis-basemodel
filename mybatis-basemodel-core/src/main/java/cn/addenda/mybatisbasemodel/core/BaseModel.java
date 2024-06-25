package cn.addenda.mybatisbasemodel.core;

import java.lang.reflect.Field;
import java.util.List;

/**
 * todo BaseModel仅仅是一个标记接口。注入字段用注解确定。
 */
public interface BaseModel {

  // todo 需要支持 跳过自动注入

  List<String> getAllFieldNameList();

  List<String> getUpdateFieldNameList();

  Field getFieldByFieldName(String fieldName);

}
