package cn.addenda.mybatisbasemodel.core.wrapper;

import cn.addenda.mybatisbasemodel.core.AdditionAttr;
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
public class AdditionWrapper<O> extends MapperMethod.ParamMap<Object> {

  public static final String ORIGINAL_PARAM_NAME = "_ORIGINAL_PARAM_NAME_@_";

  private final BaseModelELEvaluator baseModelELEvaluator;

  protected final List<AdditionAttr> additionAttrList;

  @Getter
  protected final O originalParam;

  @Setter
  private boolean fallback = false;

  public AdditionWrapper(BaseModelELEvaluator baseModelELEvaluator,
                         O originalParam, List<AdditionAttr> additionAttrList) {
    this.baseModelELEvaluator = baseModelELEvaluator;
    this.originalParam = originalParam;
    this.additionAttrList = additionAttrList;
  }

  public void init() {
    super.put(ORIGINAL_PARAM_NAME, originalParam);
    for (AdditionAttr additionAttr : additionAttrList) {
      String name = additionAttr.getName();
      if (super.containsKey(name)) {
        throw new BaseModelException(
                String.format("Parameter [%s] has existed and its corresponding value is [%s]. Current addition is [%s]. All parameters are [%s].",
                        name, get(name), additionAttr, keySet()));
      }
    }
  }

  public List<AdditionAttr> getInjectedAdditionAttrList() {
    return additionAttrList.stream()
            .filter(AdditionAttr::isIfInjected)
            .collect(Collectors.toList());
  }

  public Map<String, AdditionAttr> getInjectedAdditionAttrMap() {
    return getInjectedAdditionAttrList()
            .stream().collect(Collectors.toMap(AdditionAttr::getName, a -> a));
  }

  @Override
  public Object get(Object key) {
    try {
      return super.get(key);
    } catch (BindingException bindingException) {
      for (AdditionAttr additionAttr : additionAttrList) {
        if (additionAttr.getName().equals(key)) {
          if (!additionAttr.isIfValue()) {
            throw new BaseModelException(String.format("Current addition[%s] is not value.", additionAttr));
          }
          return additionAttr.getOrEvaluate(originalParam, baseModelELEvaluator::evaluate);
        }
      }
      if (fallback) {
        return originalParam;
      }
      throw bindingException;
    }
  }

}
