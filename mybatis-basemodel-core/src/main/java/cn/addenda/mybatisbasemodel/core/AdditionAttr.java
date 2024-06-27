package cn.addenda.mybatisbasemodel.core;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.ibatis.type.JdbcType;

import java.util.function.BiFunction;

@Setter
@Getter
@ToString
public class AdditionAttr {

  public static final String BASE_MODEL_COLUMN = "@_camelCaseToSnackCase_@";

  private String name;

  private String columnName;

  private String expression;

  private JdbcType jdbcType;

  private boolean ifValue;

  private boolean ifInjected;

  private Object value;

  public Object getOrEvaluate(Object param, BiFunction<String, Object, Object> function) {
    if (value != null) {
      return value;
    }
    this.value = function.apply(expression, param);
    return value;
  }

}
