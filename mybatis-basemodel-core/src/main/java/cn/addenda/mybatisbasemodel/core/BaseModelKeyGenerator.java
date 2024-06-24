package cn.addenda.mybatisbasemodel.core;

import cn.addenda.mybatisbasemodel.core.wrapper.AdditionalParamWrapper;
import lombok.SneakyThrows;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class BaseModelKeyGenerator extends Jdbc3KeyGenerator {

  @Override
  public void processBatch(MappedStatement ms, Statement stmt, Object parameter) {
    final String[] keyProperties = ms.getKeyProperties();
    if (keyProperties == null || keyProperties.length == 0) {
      return;
    }
    try (ResultSet rs = stmt.getGeneratedKeys()) {
      final ResultSetMetaData rsmd = rs.getMetaData();
      final Configuration configuration = ms.getConfiguration();
      if (rsmd.getColumnCount() < keyProperties.length) {
        // Error?
      } else {
        if (parameter instanceof AdditionalParamWrapper) {
          parameter = ((AdditionalParamWrapper<?>) parameter).getOriginalParam();
        }
        invokeAssignKeys(configuration, rs, rsmd, keyProperties, parameter);
      }
    } catch (Exception e) {
      throw new ExecutorException("Error getting generated key or setting result to parameter object. Cause: " + e, e);
    }
  }

  @SneakyThrows
  private void invokeAssignKeys(Configuration configuration, ResultSet rs, ResultSetMetaData rsmd, String[] keyProperties,
                                Object parameter) {
    Method assignKeys = Jdbc3KeyGenerator.class
            .getDeclaredMethod("assignKeys", Configuration.class, ResultSet.class, ResultSetMetaData.class, String[].class, Object.class);
    assignKeys.setAccessible(true);
    assignKeys.invoke(this, configuration, rs, rsmd, keyProperties, parameter);
  }

}
