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

  // ----------
  //  缓存的结果
  // ----------

  protected List<Object> cachedObj;

  protected List<Object> cachedExpression;

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
    this.setColumnName(null);
    this.setExpression(additionalParam.expression());
    this.setJdbcType(null);
    this.setIfObj(true);
    this.setIfInjected(false);
    this.setExpressionPreEvaluate(false);
    this.setObjPreEvaluate(additionalParam.objPreEvaluate());
  }

  public Object getOrEvaluateObj(Object param, BiFunction<String, Object, Object> function) {
    if (cachedObj != null) {
      if (cachedObj.size() == 1) {
        return cachedObj.get(0);
      }
      return cachedObj;
    }
    List<Object> a = new ArrayList<>();
    for (String item : expression) {
      if (objPreEvaluate) {
        a.add(function.apply(item, param));
      } else {
        a.add(item);
      }
    }
    this.cachedObj = a;
    return getOrEvaluateObj(param, function);
  }

  public Object getOrEvaluateExpression(Object param, BiFunction<String, Object, Object> function) {
    if (cachedExpression != null) {
      if (cachedExpression.size() == 1) {
        return cachedExpression.get(0);
      }
      return cachedExpression;
    }
    List<Object> a = new ArrayList<>();
    for (String item : expression) {
      if (expressionPreEvaluate) {
        a.add(function.apply(item, param));
      } else {
        a.add(item);
      }
    }
    this.cachedExpression = a;
    return getOrEvaluateExpression(param, function);
  }

}
