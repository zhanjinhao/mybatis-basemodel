package cn.addenda.mybatisbasemodel.core.wrapper;

import cn.addenda.mybatisbasemodel.core.AdditionalParamAttr;
import cn.addenda.mybatisbasemodel.core.BaseModelException;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.binding.MapperMethod;

import java.util.List;

/**
 * 只有继承了Map才能被Ognl解析
 */
@Getter
public class AdditionalParamWrapper<O> extends MapperMethod.ParamMap<Object> {

  public static final String ORIGINAL_PARAM_NAME = "_ORIGINAL_PARAM_NAME_@_";

  @Setter
  private List<AdditionalParamAttr> additionalParamAttrList;

  protected O originalParam;

  @Setter
  private boolean fallback = false;

  public AdditionalParamWrapper(O originalParam, List<AdditionalParamAttr> additionalParamAttrList) {
    this.originalParam = originalParam;
    this.additionalParamAttrList = additionalParamAttrList;
    super.put(ORIGINAL_PARAM_NAME, originalParam);
  }

  public void valid() {
    for (AdditionalParamAttr additionalParamAttr : additionalParamAttrList) {
      String name = additionalParamAttr.getName();
      if (super.containsKey(name)) {
        throw new BaseModelException(
                String.format("Parameter [%s] has existed and its corresponding value is [%s]. Current additionalParam is [%s]. All parameters are [%s].",
                        name, get(name), additionalParamAttr, keySet()));
      }
    }
  }

  @Override
  public Object get(Object key) {
    try {
      return super.get(key);
    } catch (BindingException bindingException) {
      if (fallback) {
        return originalParam;
      }
      throw bindingException;
    }
  }

  @Override
  public Object put(String key, Object value) {
    return super.put(key, value);
  }

}
