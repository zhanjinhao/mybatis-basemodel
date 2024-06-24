package cn.addenda.mybatisbasemodel.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public interface BaseModel {

  // todo 需要支持 跳过自动注入

  List<String> getAllFieldNameList();

  List<String> getUpdateFieldNameList();

  Field getFieldByFieldName(String fieldName);

}
