package cn.addenda.mybatisbasemodel.core;

public interface BaseModelSource {

  Object getValue(String fieldName, BaseModel baseModel);

  String getExpression(String fieldName, BaseModel baseModel);

  boolean ifValue(String fieldName);

}
