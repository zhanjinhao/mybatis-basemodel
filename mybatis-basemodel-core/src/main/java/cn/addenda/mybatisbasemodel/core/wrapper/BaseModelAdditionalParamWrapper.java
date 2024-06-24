package cn.addenda.mybatisbasemodel.core.wrapper;

import cn.addenda.mybatisbasemodel.core.AdditionalParamAttr;
import cn.addenda.mybatisbasemodel.core.BaseModel;

import java.lang.reflect.Field;
import java.util.List;

public class BaseModelAdditionalParamWrapper extends PojoAdditionalParamWrapper<BaseModel> implements BaseModel {

  public BaseModelAdditionalParamWrapper(BaseModel originalParam, List<AdditionalParamAttr> additionalParamAttrList) {
    super(originalParam, additionalParamAttrList);
  }

  @Override
  public List<String> getAllFieldNameList() {
    return originalParam.getAllFieldNameList();
  }

  @Override
  public List<String> getUpdateFieldNameList() {
    return originalParam.getUpdateFieldNameList();
  }

  @Override
  public Field getFieldByFieldName(String fieldName) {
    return originalParam.getFieldByFieldName(fieldName);
  }

}
