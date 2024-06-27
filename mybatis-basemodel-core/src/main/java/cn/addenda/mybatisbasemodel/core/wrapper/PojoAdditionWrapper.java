package cn.addenda.mybatisbasemodel.core.wrapper;

import cn.addenda.mybatisbasemodel.core.AdditionAttr;
import cn.addenda.mybatisbasemodel.core.BaseModelELEvaluator;
import cn.addenda.mybatisbasemodel.core.BaseModelException;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.util.List;
import java.util.stream.Collectors;

public class PojoAdditionWrapper<O> extends AdditionWrapper<O> {

  private final MetaObject metaObject;

  public PojoAdditionWrapper(BaseModelELEvaluator baseModelELEvaluator,
                             O object, List<AdditionAttr> additionAttrList) {
    super(baseModelELEvaluator, object, additionAttrList);
    this.metaObject = SystemMetaObject.forObject(object);
  }

  @Override
  public void init() {
    super.init();
    for (String key : this.keySet()) {
      if (metaObject.hasGetter(key)) {
        throw new BaseModelException(
                String.format("Parameter [%s] has existed and its corresponding value is [%s]. Current Pojo is [%s]. All parameters are [%s].",
                        key, super.get(key), originalParam, keySet()));
      }
    }
    for (AdditionAttr additionAttr : additionAttrList) {
      String name = additionAttr.getName();
      if (metaObject.hasSetter(name)) {
        throw new BaseModelException(
                String.format("Parameter [%s] has existed and its corresponding value is [%s]. Current Pojo is [%s]. All additionAttrList are %s.",
                        name, additionAttr, originalParam, additionAttrList.stream().map(AdditionAttr::getName).collect(Collectors.toList())));
      }
    }
  }

  @Override
  public Object get(Object key) {
    if (metaObject.hasGetter((String) key)) {
      return metaObject.getValue((String) key);
    }
    return super.get(key);
  }

  @Override
  public Object put(String key, Object value) {
    if (metaObject.hasSetter(key) && metaObject.hasGetter(key)) {
      Object old = metaObject.getValue(key);
      metaObject.setValue(key, value);
      return old;
    }
    return super.put(key, value);
  }

  @Override
  public boolean containsKey(Object key) {
    if (metaObject.hasGetter((String) key)) {
      return true;
    }
    return super.containsKey(key);
  }

}
