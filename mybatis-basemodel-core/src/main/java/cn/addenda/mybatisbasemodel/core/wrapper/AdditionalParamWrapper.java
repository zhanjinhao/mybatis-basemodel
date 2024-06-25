package cn.addenda.mybatisbasemodel.core.wrapper;

import cn.addenda.mybatisbasemodel.core.AdditionalParamAttr;
import cn.addenda.mybatisbasemodel.core.BaseModelELEvaluator;
import cn.addenda.mybatisbasemodel.core.BaseModelException;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.binding.MapperMethod;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 只有继承了Map才能被Ognl解析
 */
public class AdditionalParamWrapper<O> extends MapperMethod.ParamMap<Object> {

  public static final String ORIGINAL_PARAM_NAME = "_ORIGINAL_PARAM_NAME_@_";

  private final BaseModelELEvaluator baseModelELEvaluator;

  protected final List<AdditionalParamAttr> additionalParamAttrList;

  @Getter
  protected final O originalParam;

  @Setter
  private boolean fallback = false;

  public AdditionalParamWrapper(BaseModelELEvaluator baseModelELEvaluator,
                                O originalParam, List<AdditionalParamAttr> additionalParamAttrList) {
    this.baseModelELEvaluator = baseModelELEvaluator;
    this.originalParam = originalParam;
    this.additionalParamAttrList = additionalParamAttrList;
  }

  public void init() {
    super.put(ORIGINAL_PARAM_NAME, originalParam);
    for (AdditionalParamAttr additionalParamAttr : additionalParamAttrList) {
      String name = additionalParamAttr.getName();
      if (super.containsKey(name)) {
        throw new BaseModelException(
                String.format("Parameter [%s] has existed and its corresponding value is [%s]. Current additionalParam is [%s]. All parameters are [%s].",
                        name, get(name), additionalParamAttr, keySet()));
      }
    }
  }

  public List<AdditionalParamAttr> getInjectedAdditionalParamAttrList() {
    return additionalParamAttrList.stream()
            .filter(AdditionalParamAttr::isIfInjected)
            .collect(Collectors.toList());
  }

  public Map<String, AdditionalParamAttr> getInjectedAdditionalParamAttrMap() {
    return getInjectedAdditionalParamAttrList()
            .stream().collect(Collectors.toMap(AdditionalParamAttr::getName, a -> a));
  }

  @Override
  public Object get(Object key) {
    try {
      return super.get(key);
    } catch (BindingException bindingException) {
      for (AdditionalParamAttr additionalParamAttr : additionalParamAttrList) {
        if (additionalParamAttr.getName().equals(key)) {
          if (!additionalParamAttr.isIfValue()) {
            throw new BaseModelException(String.format("Current AdditionalParam[%s] is not value.", additionalParamAttr));
          }
          return additionalParamAttr.getOrEvaluate(originalParam, baseModelELEvaluator::evaluate);
        }
      }
      if (fallback) {
        return originalParam;
      }
      throw bindingException;
    }
  }

}
