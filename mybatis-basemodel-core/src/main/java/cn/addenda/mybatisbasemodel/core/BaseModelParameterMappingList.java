package cn.addenda.mybatisbasemodel.core;

import org.apache.ibatis.mapping.ParameterMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseModelParameterMappingList extends ArrayList<ParameterMapping> {

  private Map<Integer, String> propertyMap = new HashMap<>();

  @Override
  public boolean add(ParameterMapping parameterMapping) {
    return super.add(parameterMapping);
  }
}
