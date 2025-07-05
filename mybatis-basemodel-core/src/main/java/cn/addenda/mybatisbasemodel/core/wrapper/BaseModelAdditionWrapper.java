package cn.addenda.mybatisbasemodel.core.wrapper;

import cn.addenda.mybatisbasemodel.core.AdditionAttr;
import cn.addenda.mybatisbasemodel.core.BaseModel;
import cn.addenda.mybatisbasemodel.core.BaseModelAdditionAttr;
import cn.addenda.mybatisbasemodel.core.BaseModelELEvaluator;

import java.util.List;

public class BaseModelAdditionWrapper extends PojoAdditionWrapper<BaseModel> {

  public BaseModelAdditionWrapper(BaseModelELEvaluator baseModelELEvaluator,
                                  BaseModel originalParam, List<AdditionAttr> additionAttrList) {
    super(baseModelELEvaluator, originalParam, additionAttrList);
  }

  @Override
  protected boolean ifValidAttrConflictWithPojo(AdditionAttr additionAttr) {
    if (!(additionAttr instanceof BaseModelAdditionAttr)) {
      return true;
    }
    BaseModelAdditionAttr baseModelAdditionAttr = (BaseModelAdditionAttr) additionAttr;
    return !(baseModelAdditionAttr.getMetaPojo().getOriginalObject() instanceof BaseModel);
  }

}
