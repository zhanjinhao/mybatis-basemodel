package cn.addenda.mybatisbasemodel.core;

import cn.addenda.mybatisbasemodel.core.annotation.AdditionalParam;
import cn.addenda.mybatisbasemodel.core.annotation.AdditionalValue;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.ibatis.type.JdbcType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

@Setter
@Getter
@ToString
public class AdditionAttr {

  public static final String BASE_MODEL_COLUMN = "@_camelCaseToSnackCase_@";

  private String name;

  private String columnName;

  private String[] expression;

  private JdbcType jdbcType;

  private boolean ifValue;

  private boolean ifInjected;

  private boolean expressionPreEvaluate;

  private boolean valuePreEvaluate;

  private List<Object> value;

  public AdditionAttr() {
  }

  public AdditionAttr(AdditionAttr additionAttr) {
    this.setName(additionAttr.getName());
    this.setColumnName(additionAttr.getColumnName());
    this.setExpression(additionAttr.getExpression());
    this.setJdbcType(additionAttr.getJdbcType());
    this.setIfValue(additionAttr.isIfValue());
    this.setIfInjected(additionAttr.isIfInjected());
    this.setExpressionPreEvaluate(additionAttr.isExpressionPreEvaluate());
    this.setValuePreEvaluate(additionAttr.isValuePreEvaluate());
  }

  public AdditionAttr(AdditionalValue additionalValue) {
    this.setName(additionalValue.name());
    this.setColumnName(additionalValue.columnName());
    this.setExpression(new String[]{additionalValue.expression()});
    this.setJdbcType(additionalValue.jdbcType());
    this.setIfValue(additionalValue.ifValue());
    this.setIfInjected(true);
    this.setExpressionPreEvaluate(additionalValue.expressionPreEvaluate());
    this.setValuePreEvaluate(additionalValue.valuePreEvaluate());
  }

  public AdditionAttr(AdditionalParam additionalParam) {
    this.setName(additionalParam.name());
    this.setExpression(additionalParam.expression());
    this.setIfValue(true);
    this.setIfInjected(false);
    this.setExpressionPreEvaluate(false);
    this.setValuePreEvaluate(additionalParam.valuePreEvaluate());
  }

  public Object getOrEvaluate(Object param, BiFunction<String, Object, Object> function) {
    if (value != null) {
      if (value.size() == 1) {
        return value.get(0);
      }
      return value;
    }
    List<Object> a = new ArrayList<>();
    for (String item : expression) {
      if (valuePreEvaluate) {
        a.add(function.apply(item, param));
      } else {
        a.add(item);
      }
    }
    this.value = a;
    return getOrEvaluate(param, function);
  }

}
