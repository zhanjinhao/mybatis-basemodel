package cn.addenda.mybatisbasemodel.core;

import cn.addenda.mybatisbasemodel.core.annotation.AdditionalParam;
import cn.addenda.mybatisbasemodel.core.annotation.AdditionalValue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.ibatis.type.JdbcType;

import java.util.function.BiFunction;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class AdditionAttr {

  public static final String BASE_MODEL_COLUMN = "@_camelCaseToSnackCase_@";

  private String name;

  private String columnName;

  private String expression;

  private JdbcType jdbcType;

  private boolean ifValue;

  private boolean ifInjected;

  private boolean alwaysEvaluate;

  private Object value;

  public AdditionAttr(AdditionAttr additionAttr) {
    this.setName(additionAttr.getName());
    this.setColumnName(additionAttr.getColumnName());
    this.setExpression(additionAttr.getExpression());
    this.setJdbcType(additionAttr.getJdbcType());
    this.setIfValue(additionAttr.isIfValue());
    this.setIfInjected(additionAttr.isIfInjected());
    this.setAlwaysEvaluate(additionAttr.isAlwaysEvaluate());
  }

  public AdditionAttr(AdditionalValue additionalValue) {
    this.setName(additionalValue.name());
    this.setColumnName(additionalValue.columnName());
    this.setExpression(additionalValue.expression());
    this.setJdbcType(additionalValue.jdbcType());
    this.setIfValue(additionalValue.ifValue());
    this.setIfInjected(true);
    this.setAlwaysEvaluate(additionalValue.alwaysEvaluate());
  }

  public AdditionAttr(AdditionalParam additionalParam) {
    this.setName(additionalParam.name());
    this.setExpression(additionalParam.expression());
    this.setIfValue(true);
    this.setIfInjected(false);
  }

  public Object getOrEvaluate(Object param, BiFunction<String, Object, Object> function) {
    if (value != null) {
      return value;
    }
    this.value = function.apply(expression, param);
    return value;
  }

}
