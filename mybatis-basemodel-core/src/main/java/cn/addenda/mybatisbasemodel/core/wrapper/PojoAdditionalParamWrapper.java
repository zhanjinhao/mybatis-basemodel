package cn.addenda.mybatisbasemodel.core.wrapper;

import cn.addenda.mybatisbasemodel.core.AdditionalParamAttr;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.util.List;

public class PojoAdditionalParamWrapper<O> extends AdditionalParamWrapper<O> {

  private final MetaObject metaObject;

  public PojoAdditionalParamWrapper(O object, List<AdditionalParamAttr> additionalParamAttrList) {
    super(object, additionalParamAttrList);
    this.metaObject = SystemMetaObject.forObject(object);
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
