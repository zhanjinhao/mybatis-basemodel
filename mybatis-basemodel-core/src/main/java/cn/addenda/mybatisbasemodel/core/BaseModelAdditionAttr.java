package cn.addenda.mybatisbasemodel.core;

import cn.addenda.mybatisbasemodel.core.annotation.AdditionalParam;
import cn.addenda.mybatisbasemodel.core.annotation.AdditionalValue;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.ibatis.reflection.MetaObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

@Setter
@Getter
@ToString(callSuper = true)
public class BaseModelAdditionAttr extends AdditionAttr {

  // ----------------------------
  //  从pojo解析出来的数据，此字段有值
  // ----------------------------

  private MetaObject metaPojo;

  public BaseModelAdditionAttr() {
  }

  public BaseModelAdditionAttr(AdditionAttr additionAttr) {
    super(additionAttr);
  }

  public BaseModelAdditionAttr(AdditionalValue additionalValue) {
    super(additionalValue);
  }

  public BaseModelAdditionAttr(AdditionalParam additionalParam) {
    super(additionalParam);
  }

  /**
   * @return 这个方法是幂等的，即多次调用返回相同的结果
   */
  @Override
  public Object getOrEvaluateObj(Object param, BiFunction<String, Object, Object> function) {
    short fillMode = BaseModelContext.peekFillMode();
    if (fillMode == BaseModelContext.FILL_MODE_SKIP) {
      return metaPojo.getValue(getName());
    }
    if (!isIfObj()) {
      throw new UnsupportedOperationException("current attr is not obj, can not call this method.");
    }
    if (fillMode == BaseModelContext.FILL_MODE_FORCE) {
      return doGetOrEvaluateObj(param, function);
    } else if (fillMode == BaseModelContext.FILL_MODE_NULL) {
      Object value = metaPojo.getValue(getName());
      if (value == null) {
        return doGetOrEvaluateObj(param, function);
      }
      return value;
    } else if (fillMode == BaseModelContext.FILL_MODE_EMPTY) {
      Object value = metaPojo.getValue(getName());
      if (value == null || ("".equals(value))) {
        return doGetOrEvaluateObj(param, function);
      }
      return value;
    } else {
      throw new IllegalArgumentException("unsupported fill mode : %s");
    }
  }

  private Object doGetOrEvaluateObj(Object param, BiFunction<String, Object, Object> function) {
    if (cachedObj != null) {
      if (cachedObj.size() == 1) {
        if (metaPojo.hasSetter(getName())) {
          metaPojo.setValue(getName(), cachedObj.get(0));
        }
        return cachedObj.get(0);
      }
      return cachedObj;
    }
    List<Object> a = new ArrayList<>();
    for (String item : getExpression()) {
      if (isObjPreEvaluate()) {
        a.add(function.apply(item, param));
      } else {
        a.add(item);
      }
    }

    this.cachedObj = a;
    return doGetOrEvaluateObj(param, function);
  }

}
