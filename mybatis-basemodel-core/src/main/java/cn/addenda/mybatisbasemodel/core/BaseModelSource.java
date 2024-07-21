package cn.addenda.mybatisbasemodel.core;

public interface BaseModelSource {

  Object getObj(String fieldName, BaseModel baseModel);

  String getExpression(String fieldName, BaseModel baseModel);

  boolean ifObj(String fieldName);

}
