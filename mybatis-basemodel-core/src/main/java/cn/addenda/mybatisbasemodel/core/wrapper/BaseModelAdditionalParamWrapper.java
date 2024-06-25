package cn.addenda.mybatisbasemodel.core.wrapper;

import cn.addenda.mybatisbasemodel.core.AdditionalParamAttr;
import cn.addenda.mybatisbasemodel.core.BaseModel;
import cn.addenda.mybatisbasemodel.core.BaseModelELEvaluator;

import java.lang.reflect.Field;
import java.util.List;

public class BaseModelAdditionalParamWrapper extends PojoAdditionalParamWrapper<BaseModel> implements BaseModel {

  public BaseModelAdditionalParamWrapper(BaseModelELEvaluator baseModelELEvaluator,
                                         BaseModel originalParam, List<AdditionalParamAttr> additionalParamAttrList) {
    super(baseModelELEvaluator, originalParam, additionalParamAttrList);
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
