package cn.addenda.mybatisbasemodel.core;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.ibatis.type.JdbcType;

@Setter
@Getter
@ToString
public class AdditionalParamAttr {

  public static final String BASE_MODEL_COLUMN = "@_camelCaseToSnackCase_@";

  private String name;

  private String columnName;

  private String expression;

  private JdbcType jdbcType;

  private boolean ifValue;

  // todo 支持配置是否自动注入

}
