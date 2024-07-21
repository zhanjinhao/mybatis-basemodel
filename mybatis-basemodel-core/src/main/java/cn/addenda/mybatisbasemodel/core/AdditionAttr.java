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

  private boolean ifObj;

  private boolean ifInjected;

  private boolean expressionPreEvaluate;

  private boolean objPreEvaluate;

  private List<Object> obj;

  public AdditionAttr() {
  }

  public AdditionAttr(AdditionAttr additionAttr) {
    this.setName(additionAttr.getName());
    this.setColumnName(additionAttr.getColumnName());
    this.setExpression(additionAttr.getExpression());
    this.setJdbcType(additionAttr.getJdbcType());
    this.setIfObj(additionAttr.isIfObj());
    this.setIfInjected(additionAttr.isIfInjected());
    this.setExpressionPreEvaluate(additionAttr.isExpressionPreEvaluate());
    this.setObjPreEvaluate(additionAttr.isObjPreEvaluate());
  }

  public AdditionAttr(AdditionalValue additionalValue) {
    this.setName(additionalValue.name());
    this.setColumnName(additionalValue.columnName());
    this.setExpression(new String[]{additionalValue.expression()});
    this.setJdbcType(additionalValue.jdbcType());
    this.setIfObj(additionalValue.ifObj());
    this.setIfInjected(true);
    this.setExpressionPreEvaluate(additionalValue.expressionPreEvaluate());
    this.setObjPreEvaluate(additionalValue.objPreEvaluate());
  }

  public AdditionAttr(AdditionalParam additionalParam) {
    this.setName(additionalParam.name());
    this.setExpression(additionalParam.expression());
    this.setIfObj(true);
    this.setIfInjected(false);
    this.setExpressionPreEvaluate(false);
    this.setObjPreEvaluate(additionalParam.objPreEvaluate());
  }

  public Object getOrEvaluate(Object param, BiFunction<String, Object, Object> function) {
    if (obj != null) {
      if (obj.size() == 1) {
        return obj.get(0);
      }
      return obj;
    }
    List<Object> a = new ArrayList<>();
    for (String item : expression) {
      if (objPreEvaluate) {
        a.add(function.apply(item, param));
      } else {
        a.add(item);
      }
    }
    this.obj = a;
    return getOrEvaluate(param, function);
  }

}
