package cn.addenda.mybatisbasemodel.core;

import lombok.Getter;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;

import java.util.List;

public class BaseModelBoundSql extends BoundSql {

  @Getter
  private final String originalSql;

  public BaseModelBoundSql(Configuration configuration, String sql,
                           List<ParameterMapping> parameterMappings, Object parameterObject, String originalSql) {
    super(configuration, sql, parameterMappings, parameterObject);
    this.originalSql = originalSql;
  }

}
