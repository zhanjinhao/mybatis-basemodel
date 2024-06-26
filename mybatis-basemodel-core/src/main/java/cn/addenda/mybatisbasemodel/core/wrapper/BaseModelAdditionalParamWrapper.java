package cn.addenda.mybatisbasemodel.core.wrapper;

import cn.addenda.mybatisbasemodel.core.AdditionalParamAttr;
import cn.addenda.mybatisbasemodel.core.BaseModel;
import cn.addenda.mybatisbasemodel.core.BaseModelAdapter;
import cn.addenda.mybatisbasemodel.core.BaseModelELEvaluator;

import java.util.List;

public class BaseModelAdditionalParamWrapper extends PojoAdditionalParamWrapper<BaseModel> implements BaseModelAdapter {

  public BaseModelAdditionalParamWrapper(BaseModelELEvaluator baseModelELEvaluator,
                                         BaseModel originalParam, List<AdditionalParamAttr> additionalParamAttrList) {
    super(baseModelELEvaluator, originalParam, additionalParamAttrList);
  }

  @Override
  public BaseModel getDelegate() {
    return originalParam;
  }
}
