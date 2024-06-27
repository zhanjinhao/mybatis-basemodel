package cn.addenda.mybatisbasemodel.core.wrapper;

import cn.addenda.mybatisbasemodel.core.AdditionAttr;
import cn.addenda.mybatisbasemodel.core.BaseModel;
import cn.addenda.mybatisbasemodel.core.BaseModelAdapter;
import cn.addenda.mybatisbasemodel.core.BaseModelELEvaluator;

import java.util.List;

public class BaseModelAdditionWrapper extends PojoAdditionWrapper<BaseModel> implements BaseModelAdapter {

  public BaseModelAdditionWrapper(BaseModelELEvaluator baseModelELEvaluator,
                                  BaseModel originalParam, List<AdditionAttr> additionAttrList) {
    super(baseModelELEvaluator, originalParam, additionAttrList);
  }

  @Override
  public BaseModel getDelegate() {
    return originalParam;
  }
}
