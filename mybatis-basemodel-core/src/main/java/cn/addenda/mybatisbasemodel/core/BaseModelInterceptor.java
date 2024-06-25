package cn.addenda.mybatisbasemodel.core;

import cn.addenda.mybatisbasemodel.core.annotation.AdditionalParam;
import cn.addenda.mybatisbasemodel.core.annotation.BaseModelColumnName;
import cn.addenda.mybatisbasemodel.core.annotation.BaseModelJdbcType;
import cn.addenda.mybatisbasemodel.core.util.InstanceUtils;
import cn.addenda.mybatisbasemodel.core.util.JSqlParserUtils;
import cn.addenda.mybatisbasemodel.core.util.PluginUtils;
import cn.addenda.mybatisbasemodel.core.util.ProxyUtils;
import cn.addenda.mybatisbasemodel.core.wrapper.AdditionalParamWrapper;
import cn.addenda.mybatisbasemodel.core.wrapper.BaseModelAdditionalParamWrapper;
import cn.addenda.mybatisbasemodel.core.wrapper.PojoAdditionalParamWrapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.defaults.DefaultSqlSession;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.ibatis.executor.keygen.SelectKeyGenerator.SELECT_KEY_SUFFIX;

@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
        @Signature(type = StatementHandler.class, method = "parameterize", args = {java.sql.Statement.class}),

        @Signature(type = ParameterHandler.class, method = "setParameters", args = {java.sql.PreparedStatement.class}),

        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "queryCursor", args = {MappedStatement.class, Object.class, RowBounds.class})
})
@Slf4j
public class BaseModelInterceptor implements Interceptor {
  private BaseModelELEvaluator baseModelELEvaluator;
  private static final String BASE_MODEL_EL_EVALUATOR_NAME = "baseModelELEvaluator";
  private BaseModelSource baseModelSource;
  private static final String BASE_MODEL_SOURCE_NAME = "baseModelSource";

  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    Object actualTarget = ProxyUtils.resolveActualObject(invocation.getTarget());

    if (actualTarget instanceof StatementHandler) {
      return interceptStatementHandler((StatementHandler) actualTarget, invocation);
    }

    if (actualTarget instanceof ParameterHandler) {
      return interceptParameterHandler((ParameterHandler) actualTarget, invocation);
    }

    if (actualTarget instanceof Executor) {
      return interceptExecutor((Executor) actualTarget, invocation);
    }

    return invocation.proceed();
  }

  private Object interceptStatementHandler(StatementHandler statementHandler, Invocation invocation) throws Throwable {
    Method method = invocation.getMethod();
    if (!"prepare".equals(method.getName()) && !"parameterize".equals(method.getName())) {
      return invocation.proceed();
    }

    MetaObject statementHandlerMetaObject = SystemMetaObject.forObject(statementHandler);
    MappedStatement mappedStatement = (MappedStatement) statementHandlerMetaObject.getValue("delegate.mappedStatement");
    SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
    if (sqlCommandType == null || sqlCommandType == SqlCommandType.UNKNOWN || sqlCommandType == SqlCommandType.FLUSH
            || sqlCommandType == SqlCommandType.DELETE || sqlCommandType == SqlCommandType.SELECT) {
      return invocation.proceed();
    }
    BoundSql boundSql = statementHandler.getBoundSql();

    if ("prepare".equals(method.getName())) {
      if (sqlCommandType == SqlCommandType.INSERT) {
        Object parameterObject = boundSql.getParameterObject();
        if (parameterObject instanceof BaseModel) {
          rewriteInsertSql(statementHandlerMetaObject, mappedStatement, (BaseModel) parameterObject);
        }
        if (parameterObject instanceof AdditionalParamWrapper) {
          rewriteInsertSql(statementHandlerMetaObject, mappedStatement, (AdditionalParamWrapper<?>) parameterObject);
        }
      } else if (sqlCommandType == SqlCommandType.UPDATE) {
        Object parameterObject = boundSql.getParameterObject();
        if (parameterObject instanceof BaseModel) {
          rewriteUpdateSql(statementHandlerMetaObject, mappedStatement, (BaseModel) parameterObject);
        }
        if (parameterObject instanceof AdditionalParamWrapper) {
          rewriteUpdateSql(statementHandlerMetaObject, mappedStatement, (AdditionalParamWrapper<?>) parameterObject);
        }
      }
    } else if ("parameterize".equals(method.getName())) {
      if (sqlCommandType == SqlCommandType.INSERT) {
        Object parameterObject = boundSql.getParameterObject();
        if (parameterObject instanceof BaseModel) {
          injectInsertSql(statementHandlerMetaObject, mappedStatement, (BaseModel) parameterObject);
        }
        if (parameterObject instanceof AdditionalParamWrapper) {
          injectInsertSql(statementHandlerMetaObject, mappedStatement, (AdditionalParamWrapper<?>) parameterObject);
        }
      } else if (sqlCommandType == SqlCommandType.UPDATE) {
        Object parameterObject = boundSql.getParameterObject();
        if (parameterObject instanceof BaseModel) {
          injectUpdateSql(statementHandlerMetaObject, mappedStatement, (BaseModel) parameterObject);
        }
        if (parameterObject instanceof AdditionalParamWrapper) {
          injectUpdateSql(statementHandlerMetaObject, mappedStatement, (AdditionalParamWrapper<?>) parameterObject);
        }
      }
    } else {
      throw new UnsupportedOperationException(method.toString());
    }
    return invocation.proceed();
  }

  private void rewriteInsertSql(MetaObject statementHandlerMetaObject, MappedStatement mappedStatement, AdditionalParamWrapper<?> additionalParamWrapper) {
    StatementHandler statementHandler = (StatementHandler) statementHandlerMetaObject.getOriginalObject();
    BoundSql boundSql = statementHandler.getBoundSql();
    Insert insert = parseAndGetStatement(statementHandler, Insert.class);

    String newSql = doRewriteSql(mappedStatement, additionalParamWrapper, JSqlParserUtils.wrap(insert));
    replaceSql(statementHandlerMetaObject, boundSql, mappedStatement, newSql);
  }

  private void rewriteUpdateSql(MetaObject statementHandlerMetaObject, MappedStatement mappedStatement, AdditionalParamWrapper<?> additionalParamWrapper) {
    StatementHandler statementHandler = (StatementHandler) statementHandlerMetaObject.getOriginalObject();
    BoundSql boundSql = statementHandler.getBoundSql();
    Update update = parseAndGetStatement(statementHandler, Update.class);

    String newSql = doRewriteSql(mappedStatement, additionalParamWrapper, JSqlParserUtils.wrap(update));
    replaceSql(statementHandlerMetaObject, boundSql, mappedStatement, newSql);
  }

  private String doRewriteSql(MappedStatement mappedStatement, AdditionalParamWrapper<?> additionalParamWrapper,
                              JSqlParserStatementWrapper jSqlParserStatementWrapper) {
    List<Column> columnList = jSqlParserStatementWrapper.getColumnList();
    Map<String, AdditionalParamAttr> additionalParamMap = additionalParamWrapper.getInjectedAdditionalParamAttrMap();
    List<String> columnNameList = formatColumnName(columnList);
    for (Map.Entry<String, AdditionalParamAttr> entry : additionalParamMap.entrySet()) {
      AdditionalParamAttr additionalParamAttr = entry.getValue();
      String columnName = calculateColumnName(additionalParamAttr, columnNameList);
      if (columnName == null) {
        continue;
      }

      if (!additionalParamAttr.isIfValue()) {
        // todo 需不需要解析一下？Object evaluate = baseModelELEvaluator.evaluate(additionalParamAttr.el(), additionalParamAttr);
        Expression expression = JSqlParserUtils.parseExpression(additionalParamAttr.getExpression());
        if (expression == null) {
          throw new UnsupportedOperationException(
                  String.format("mappedStatement [%s], can not parse expression from [%s], current name is [%s].",
                          mappedStatement.getId(), additionalParamAttr.getExpression(), additionalParamAttr.getName()));
        }
        jSqlParserStatementWrapper.addColumn(new Column(columnName), expression);
      } else {
        jSqlParserStatementWrapper.addColumn(new Column(columnName), new JdbcParameter());
      }
    }

    return jSqlParserStatementWrapper.toString();
  }

  private void injectInsertSql(MetaObject statementHandlerMetaObject, MappedStatement mappedStatement, AdditionalParamWrapper<?> additionalParamWrapper) {
    StatementHandler statementHandler = (StatementHandler) statementHandlerMetaObject.getOriginalObject();
    Insert insert = parseAndGetStatement2(statementHandler, Insert.class);

    List<ParameterMapping> injectedParameterMappingList = generateParameterMapping(mappedStatement, additionalParamWrapper, JSqlParserUtils.wrap(insert));
    replaceParameterMapping(statementHandler, mappedStatement, injectedParameterMappingList, 0);
  }

  private void injectUpdateSql(MetaObject statementHandlerMetaObject, MappedStatement mappedStatement, AdditionalParamWrapper<?> additionalParamWrapper) {
    StatementHandler statementHandler = (StatementHandler) statementHandlerMetaObject.getOriginalObject();
    Update update = parseAndGetStatement2(statementHandler, Update.class);

    List<ParameterMapping> injectedParameterMappingList = generateParameterMapping(mappedStatement, additionalParamWrapper, JSqlParserUtils.wrap(update));
    replaceParameterMapping(statementHandler, mappedStatement, injectedParameterMappingList, countOccurrencesOf(update.getWhere().toString(), "?"));
  }

  private void replaceParameterMapping(StatementHandler statementHandler, MappedStatement mappedStatement,
                                       List<ParameterMapping> injectedParameterMappingList, int insertIndexOffset) {
    // 对于DynamicSqlSource，currentParameterMappingList是从StaticSqlSource#parameterMappings拿的，每次拿的都是同一个对象
    // 对于RawSqlSource，currentParameterMappingList是每次调用DynamicSqlSource#getBoundSql时创建的，每次都是新的对象。

    // org.apache.ibatis.executor.statement.BaseStatementHandler.boundSql
    // org.apache.ibatis.scripting.defaults.DefaultParameterHandler.boundSql
    // org.apache.ibatis.executor.resultset.DefaultResultSetHandler.boundSql
    // 在一个BaseStatementHandler中，这三个引用指向同一个对象。所以：

    // 对于DynamicSqlSource，只要在BoundSql#parameterMappings里增加即可。
    // 对于RawSqlSource，需要替换掉BoundSql#parameterMappings，否则会影响StaticSqlSource#parameterMappings。

    SqlSource sqlSource = mappedStatement.getSqlSource();

    if (!injectedParameterMappingList.isEmpty()) {
      BoundSql boundSql = statementHandler.getBoundSql();
      List<ParameterMapping> currentParameterMappingList = boundSql.getParameterMappings();
      // insert into t set name = ?, age = ?
      // update t set name = ? where id = ?
      int insertIndex = currentParameterMappingList.size() - insertIndexOffset;
      if (sqlSource instanceof RawSqlSource) {
        List<ParameterMapping> parameterMappingList = new ArrayList<>(currentParameterMappingList);
        for (ParameterMapping parameterMapping : injectedParameterMappingList) {
          parameterMappingList.add(insertIndex, parameterMapping);
        }
        MetaObject metaObject = mappedStatement.getConfiguration().newMetaObject(boundSql);
        metaObject.setValue("parameterMappings", parameterMappingList);
      } else if (sqlSource instanceof DynamicSqlSource) {
        for (ParameterMapping parameterMapping : injectedParameterMappingList) {
          currentParameterMappingList.add(insertIndex, parameterMapping);
        }
      }
    }
  }

  private List<ParameterMapping> generateParameterMapping(MappedStatement mappedStatement, AdditionalParamWrapper<?> additionalParamWrapper,
                                                          JSqlParserStatementWrapper jSqlParserStatementWrapper) {
    Configuration configuration = mappedStatement.getConfiguration();

    List<Column> columnList = jSqlParserStatementWrapper.getColumnList();
    Map<String, AdditionalParamAttr> additionalParamMap = additionalParamWrapper.getInjectedAdditionalParamAttrMap();
    List<String> columnNameList = formatColumnName(columnList);
    List<ParameterMapping> parameterMappingList = new ArrayList<>();
    for (Map.Entry<String, AdditionalParamAttr> entry : additionalParamMap.entrySet()) {
      AdditionalParamAttr additionalParamAttr = entry.getValue();
      String columnName = calculateColumnName(additionalParamAttr, columnNameList);
      if (columnName == null) {
        continue;
      }

      if (additionalParamAttr.isIfValue()) {
        ParameterMapping parameterMapping = buildParameterMapping(additionalParamAttr.getJdbcType(), additionalParamAttr.getName(), configuration);
        parameterMappingList.add(parameterMapping);
      }
    }
    return parameterMappingList;
  }

  private void rewriteInsertSql(MetaObject statementHandlerMetaObject, MappedStatement mappedStatement, BaseModel baseModel) {
    StatementHandler statementHandler = (StatementHandler) statementHandlerMetaObject.getOriginalObject();
    BoundSql boundSql = statementHandler.getBoundSql();
    Insert insert = parseAndGetStatement(statementHandler, Insert.class);

    String newSql = doRewriteSql(mappedStatement, baseModel.getAllFieldNameList(), baseModel, JSqlParserUtils.wrap(insert));
    replaceSql(statementHandlerMetaObject, boundSql, mappedStatement, newSql);
  }

  private void rewriteUpdateSql(MetaObject statementHandlerMetaObject, MappedStatement mappedStatement, BaseModel baseModel) {
    StatementHandler statementHandler = (StatementHandler) statementHandlerMetaObject.getOriginalObject();
    BoundSql boundSql = statementHandler.getBoundSql();
    Update update = parseAndGetStatement(statementHandler, Update.class);

    String newSql = doRewriteSql(mappedStatement, baseModel.getUpdateFieldNameList(), baseModel, JSqlParserUtils.wrap(update));
    replaceSql(statementHandlerMetaObject, boundSql, mappedStatement, newSql);
  }

  private String doRewriteSql(MappedStatement mappedStatement, List<String> injectedFieldNameList, BaseModel baseModel,
                              JSqlParserStatementWrapper jSqlParserStatementWrapper) {
    List<Column> columnList = jSqlParserStatementWrapper.getColumnList();
    List<String> columnNameList = formatColumnName(columnList);

    for (String fieldName : injectedFieldNameList) {
      Field field = baseModel.getFieldByFieldName(fieldName);
      String columnName = calculateColumnName(field, columnNameList);
      if (columnName == null) {
        continue;
      }

      if (!baseModelSource.ifValue(fieldName)) {
        String expressionStr = baseModelSource.getExpression(fieldName, baseModel);
        Expression expression = JSqlParserUtils.parseExpression(expressionStr);
        if (expression == null) {
          throw new UnsupportedOperationException(
                  String.format("mappedStatement [%s], can not parse expression from [%s], current fieldName is [%s].",
                          mappedStatement.getId(), expressionStr, fieldName));
        }
        jSqlParserStatementWrapper.addColumn(new Column(columnName), expression);
      } else {
        jSqlParserStatementWrapper.addColumn(new Column(columnName), new JdbcParameter());
      }
    }

    return jSqlParserStatementWrapper.toString();
  }

  private void injectInsertSql(MetaObject statementHandlerMetaObject, MappedStatement mappedStatement, BaseModel baseModel) {
    StatementHandler statementHandler = (StatementHandler) statementHandlerMetaObject.getOriginalObject();
    Insert insert = parseAndGetStatement2(statementHandler, Insert.class);

    List<ParameterMapping> injectedParameterMappings = generateParameterMapping(mappedStatement, baseModel.getAllFieldNameList(), baseModel, JSqlParserUtils.wrap(insert));
    replaceParameterMapping(statementHandler, mappedStatement, injectedParameterMappings, 0);
  }

  private void injectUpdateSql(MetaObject statementHandlerMetaObject, MappedStatement mappedStatement, BaseModel baseModel) {
    StatementHandler statementHandler = (StatementHandler) statementHandlerMetaObject.getOriginalObject();
    Update update = parseAndGetStatement2(statementHandler, Update.class);

    List<ParameterMapping> injectedParameterMappings = generateParameterMapping(mappedStatement, baseModel.getUpdateFieldNameList(), baseModel, JSqlParserUtils.wrap(update));
    replaceParameterMapping(statementHandler, mappedStatement, injectedParameterMappings, countOccurrencesOf(update.getWhere().toString(), "?"));
  }

  private List<ParameterMapping> generateParameterMapping(MappedStatement mappedStatement, List<String> injectedFieldNameList, BaseModel baseModel,
                                                          JSqlParserStatementWrapper jSqlParserStatementWrapper) {
    Configuration configuration = mappedStatement.getConfiguration();
    List<Column> columnList = jSqlParserStatementWrapper.getColumnList();
    List<String> columnNameList = formatColumnName(columnList);

    List<ParameterMapping> parameterMappingList = new ArrayList<>();
    for (String fieldName : injectedFieldNameList) {
      Field field = baseModel.getFieldByFieldName(fieldName);
      String columnName = calculateColumnName(field, columnNameList);
      if (columnName == null) {
        continue;
      }

      if (baseModelSource.ifValue(fieldName)) {
        ParameterMapping parameterMapping = buildParameterMapping(field, fieldName, configuration);
        parameterMappingList.add(parameterMapping);
      }
    }
    return parameterMappingList;
  }

  private void replaceSql(MetaObject statementHandlerMetaObject, BoundSql boundSql, MappedStatement mappedStatement, String newSql) {
    Configuration configuration = mappedStatement.getConfiguration();
    if (boundSql instanceof BaseModelBoundSql) {
      statementHandlerMetaObject.setValue("delegate.boundSql.sql", newSql);
    } else {
      BaseModelBoundSql baseModelBoundSql = new BaseModelBoundSql(
              configuration, newSql, boundSql.getParameterMappings(), boundSql.getParameterObject(), boundSql.getSql());
      statementHandlerMetaObject.setValue("delegate.boundSql", baseModelBoundSql);

      // org.apache.ibatis.executor.statement.BaseStatementHandler.boundSql
      // org.apache.ibatis.scripting.defaults.DefaultParameterHandler.boundSql
      // org.apache.ibatis.executor.resultset.DefaultResultSetHandler.boundSql
      // 在一个BaseStatementHandler中，这三个引用指向同一个对象。所以：
      ParameterHandler parameterHandler = ProxyUtils.resolveActualObject(statementHandlerMetaObject.getValue("delegate.parameterHandler"));
      configuration.newMetaObject(parameterHandler).setValue("boundSql", baseModelBoundSql);
      ResultSetHandler resultHandler = ProxyUtils.resolveActualObject(statementHandlerMetaObject.getValue("delegate.resultSetHandler"));
      configuration.newMetaObject(resultHandler).setValue("boundSql", baseModelBoundSql);
    }
  }

  private <T extends Statement> T parseAndGetStatement(StatementHandler statementHandler, Class<T> clazz) {
    BoundSql boundSql = statementHandler.getBoundSql();
    Statement statement = JSqlParserUtils.parseAndGetStatement(boundSql.getSql());

    if (!clazz.isAssignableFrom(statement.getClass())) {
      throw new UnsupportedOperationException(String.format("Unsupported sql : [%s].", boundSql.getSql()));
    }

    return (T) statement;
  }

  private <T extends Statement> T parseAndGetStatement2(StatementHandler statementHandler, Class<T> clazz) {
    BoundSql boundSql = statementHandler.getBoundSql();
    Statement statement;
    if (boundSql instanceof BaseModelBoundSql) {
      statement = JSqlParserUtils.parseAndGetStatement(((BaseModelBoundSql) boundSql).getOriginalSql());
    } else {
      statement = JSqlParserUtils.parseAndGetStatement(boundSql.getSql());
    }

    if (!clazz.isAssignableFrom(statement.getClass())) {
      throw new UnsupportedOperationException(String.format("Unsupported sql : [%s].", boundSql.getSql()));
    }

    return (T) statement;
  }

  private String calculateColumnName(Field field, List<String> columnNameList) {
    String fieldName = field.getName();
    String columnName = getColumnName(field);
    if (null == columnName) {
      columnName = camelCaseToSnakeCase(fieldName);
    }

    // 属性去重，对于用户已经添加的字段，不能再自动注入
    if (columnNameList.contains(columnName)) {
      return null;
    }

    return columnName;
  }

  private String calculateColumnName(AdditionalParamAttr additionalParamAttr, List<String> columnNameList) {
    String fieldName = additionalParamAttr.getName();

    String columnName = additionalParamAttr.getColumnName();
    if (AdditionalParamAttr.BASE_MODEL_COLUMN.equals(columnName)) {
      columnName = camelCaseToSnakeCase(fieldName);
    }
    // 属性去重，对于用户已经添加的字段，不能再自动注入
    if (columnNameList.contains(columnName)) {
      return null;
    }

    return columnName;
  }

  private String getColumnName(Field field) {
    BaseModelColumnName annotation = field.getAnnotation(BaseModelColumnName.class);
    if (annotation == null) {
      return null;
    }
    return annotation.value();
  }

  private JdbcType getJdbcType(Field field) {
    BaseModelJdbcType annotation = field.getAnnotation(BaseModelJdbcType.class);
    if (annotation == null) {
      return null;
    }
    return annotation.value();
  }

  private Object interceptParameterHandler(ParameterHandler parameterHandler, Invocation invocation) throws Throwable {
    if (!(parameterHandler instanceof DefaultParameterHandler)) {
      throw new UnsupportedOperationException(String.format("Unsupported ParameterHandler. type: [%s], obj: [%s].",
              parameterHandler.getClass(), parameterHandler));
    }

    Object parameterObject = parameterHandler.getParameterObject();
    MetaObject metaObject = SystemMetaObject.forObject(parameterHandler);
    if (parameterObject instanceof BaseModel) {
      MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("mappedStatement");
      SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
      if (sqlCommandType == SqlCommandType.INSERT) {
        fillInsert((BaseModel) parameterObject, mappedStatement.getConfiguration());
      } else if (sqlCommandType == SqlCommandType.UPDATE) {
        fillUpdate((BaseModel) parameterObject, mappedStatement.getConfiguration());
      }
    }

    if (!(parameterObject instanceof AdditionalParamWrapper)) {
      return invocation.proceed();
    }

    BoundSql boundSql = (BoundSql) metaObject.getValue("boundSql");

    AdditionalParamWrapper<?> additionalParamWrapper = (AdditionalParamWrapper<?>) parameterObject;
    List<AdditionalParamAttr> additionalParamAttrList = additionalParamWrapper.getInjectedAdditionalParamAttrList();
    try {
      for (AdditionalParamAttr additionalParamAttr : additionalParamAttrList) {
        if (additionalParamAttr.isIfValue()) {
          boundSql.setAdditionalParameter(additionalParamAttr.getName(),
                  additionalParamAttr.getOrEvaluate(additionalParamWrapper.getOriginalParam(), baseModelELEvaluator::evaluate));
        }
      }
      boundSql.setAdditionalParameter(AdditionalParamWrapper.ORIGINAL_PARAM_NAME, additionalParamWrapper.getOriginalParam());
      return invocation.proceed();
    } finally {
      for (AdditionalParamAttr additionalParamAttr : additionalParamAttrList) {
        if (additionalParamAttr.isIfValue()) {
          boundSql.getAdditionalParameters().remove(additionalParamAttr.getName());
        }
      }
      boundSql.getAdditionalParameters().remove(AdditionalParamWrapper.ORIGINAL_PARAM_NAME);
    }
  }

  private Object interceptExecutor(Executor executor, Invocation invocation) throws Throwable {
    Object[] args = invocation.getArgs();
    MappedStatement mappedStatement = (MappedStatement) args[0];
    String msId = mappedStatement.getId();

    AdditionalParam[] additionalParams = extractAdditionalParam(msId);
    if (additionalParams != null && additionalParams.length != 0) {
      // 观察org.apache.ibatis.reflection.ParamNameResolver.getNamedParams的实现可知，返回值一共有三种类型
      // - 没有参数时返回null
      // - 有一个参数且没有@Param注解时返回参数本身
      // - 返回MapperMethod.ParamMap
      Object arg = args[1];
      if (arg == null) {
        AdditionalParamWrapper<?> paramWrapper = new AdditionalParamWrapper<>(baseModelELEvaluator,
                null, Arrays.stream(additionalParams).map(this::toAttr).collect(Collectors.toList()));
        paramWrapper.init();
        args[1] = paramWrapper;
      } else {
        if (arg instanceof MapperMethod.ParamMap) {
          AdditionalParamWrapper<?> paramWrapper = new AdditionalParamWrapper<>(baseModelELEvaluator,
                  arg, Arrays.stream(additionalParams).map(this::toAttr).collect(Collectors.toList()));
          paramWrapper.putAll((MapperMethod.ParamMap<?>) arg);
          paramWrapper.init();
          args[1] = paramWrapper;
        }
        // StrictMap在3.5.5之前是 org.apache.ibatis.session.defaults.DefaultSqlSession.wrapCollection() 的返回值。兼容一下。
        else if (arg instanceof DefaultSqlSession.StrictMap) {
          AdditionalParamWrapper<?> paramWrapper = new AdditionalParamWrapper<>(baseModelELEvaluator,
                  arg, Arrays.stream(additionalParams).map(this::toAttr).collect(Collectors.toList()));
          paramWrapper.putAll((DefaultSqlSession.StrictMap<?>) arg);
          paramWrapper.init();
          args[1] = paramWrapper;
        }
        // 一个参数且没有@Param时走这个地方
        else {
          TypeHandlerRegistry typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
          if (typeHandlerRegistry.hasTypeHandler(arg.getClass())) {
            AdditionalParamWrapper<?> paramWrapper = new AdditionalParamWrapper<>(baseModelELEvaluator,
                    arg, Arrays.stream(additionalParams).map(this::toAttr).collect(Collectors.toList()));
            paramWrapper.setFallback(true);
            paramWrapper.init();
            args[1] = paramWrapper;
          } else if (arg instanceof BaseModel) {
            BaseModel baseModel = (BaseModel) arg;
            BaseModelAdditionalParamWrapper paramWrapper = new BaseModelAdditionalParamWrapper(baseModelELEvaluator,
                    baseModel, Arrays.stream(additionalParams).map(this::toAttr).collect(Collectors.toList()));
            paramWrapper.init();
            replaceKeyGenerator(executor, mappedStatement);
            args[1] = paramWrapper;
          } else {
            PojoAdditionalParamWrapper<?> paramWrapper = new PojoAdditionalParamWrapper<>(baseModelELEvaluator,
                    arg, Arrays.stream(additionalParams).map(this::toAttr).collect(Collectors.toList()));
            paramWrapper.init();
            replaceKeyGenerator(executor, mappedStatement);
            args[1] = paramWrapper;
          }
        }
      }
    }

    return invocation.proceed();
  }

  private void replaceKeyGenerator(Executor executor, MappedStatement mappedStatement) {
    if (PluginUtils.isBatchMode(executor)) {
      return;
    }
    if (mappedStatement.getKeyProperties() == null || mappedStatement.getKeyProperties().length == 0) {
      return;
    }
    MetaObject metaObject = mappedStatement.getConfiguration().newMetaObject(mappedStatement);
    Object keyGenerator = metaObject.getValue("keyGenerator");
    if (keyGenerator instanceof BaseModelKeyGenerator) {
      return;
    }
    BaseModelKeyGenerator baseModelKeyGenerator = new BaseModelKeyGenerator();
    metaObject.setValue("keyGenerator", baseModelKeyGenerator);
  }

  private AdditionalParamAttr toAttr(AdditionalParam additionalParam) {
    AdditionalParamAttr additionalParamAttr = new AdditionalParamAttr();
    additionalParamAttr.setName(additionalParam.name());
    additionalParamAttr.setColumnName(additionalParam.columnName());
    additionalParamAttr.setExpression(additionalParam.expression());
    additionalParamAttr.setJdbcType(additionalParam.jdbcType());
    additionalParamAttr.setIfValue(additionalParam.ifValue());
    additionalParamAttr.setIfInjected(additionalParam.ifInjected());
    return additionalParamAttr;
  }

  private static final Map<String, AdditionalParam[]> ADDITINAL_PARAM_MAP = new ConcurrentHashMap<>();

  private AdditionalParam[] extractAdditionalParam(String msId) {
    if (msId.endsWith(SELECT_KEY_SUFFIX)) {
      return new AdditionalParam[0];
    }
    return ADDITINAL_PARAM_MAP.computeIfAbsent(msId,
            new Function<String, AdditionalParam[]>() {
              @Override
              @SneakyThrows
              public AdditionalParam[] apply(String s) {
                int end = msId.lastIndexOf(".");
                Class<?> aClass = Class.forName(msId.substring(0, end));
                String methodName = msId.substring(end + 1);
                Method[] methods = aClass.getMethods();
                for (Method method : methods) {
                  // mybatis 动态代理模式不支持函数重载。用方法名匹配没问题。
                  if (method.getName().equals(methodName)) {
                    AdditionalParam[] additionalParams = method.getAnnotationsByType(AdditionalParam.class);
                    validRepeat(msId, additionalParams);
                    return additionalParams;
                  }
                }
                throw new BaseModelException(String.format("Can not extract AdditionalParam from MappedStatement[%s]！", msId));
              }
            });
  }

  private void validRepeat(String msId, AdditionalParam[] additionalParams) {
    Set<String> additionalParamNameSet = new HashSet<>();
    for (AdditionalParam additionalParam : additionalParams) {
      String name = additionalParam.name();
      if (additionalParamNameSet.contains(name)) {
        throw new BaseModelException(String.format("AdditionalParam[%s] of MappedStatement[%s] repeat.", name, msId));
      }
      additionalParamNameSet.add(name);
    }
  }

  @Override
  public void setProperties(Properties properties) {
    if (baseModelELEvaluator == null) {
      if (properties.containsKey(BASE_MODEL_EL_EVALUATOR_NAME)) {
        String baseModelElEvaluatorName = (String) properties.get(BASE_MODEL_EL_EVALUATOR_NAME);
        if (baseModelElEvaluatorName != null) {
          baseModelELEvaluator = InstanceUtils.newInstance(baseModelElEvaluatorName, BaseModelELEvaluator.class);
        }
      } else {
        String msg = String.format("[%s] of [%s] can not be null！", BASE_MODEL_EL_EVALUATOR_NAME, this.getClass());
        throw new BaseModelException(msg);
      }
    }
    if (baseModelSource == null) {
      if (properties.containsKey(BASE_MODEL_SOURCE_NAME)) {
        String baseModelSourceName = (String) properties.get(BASE_MODEL_SOURCE_NAME);
        if (baseModelSourceName != null) {
          baseModelSource = InstanceUtils.newInstance(baseModelSourceName, BaseModelSource.class);
        }
      } else {
        String msg = String.format("[%s] of [%s] can not be null！", BASE_MODEL_SOURCE_NAME, this.getClass());
        throw new BaseModelException(msg);
      }
    }
  }


  /**
   * 针对BaseModel的一个属性创建ParameterMapping对象
   * org.apache.ibatis.type.JdbcType
   *
   * @param field BaseModel属性字段
   */
  private ParameterMapping buildParameterMapping(Field field, String fieldName, Configuration configuration) {
    if (field == null) {
      throw new NullPointerException(String.format("field [%s] 为空！", fieldName));
    }
    Class<?> propertyType = field.getType();
    ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, fieldName, propertyType);

    JdbcType jdbcType = getJdbcType(field);
    if (jdbcType != null) {
      builder.jdbcType(jdbcType);
      return builder.build();
    }

    if (propertyType == boolean.class || propertyType == Boolean.class) {
      jdbcType = JdbcType.BOOLEAN;
    } else if (propertyType == Byte.class || propertyType == byte.class) {
      jdbcType = JdbcType.TINYINT;
    } else if (propertyType == Short.class || propertyType == short.class) {
      jdbcType = JdbcType.SMALLINT;
    } else if (propertyType == Integer.class || propertyType == int.class) {
      jdbcType = JdbcType.INTEGER;
    } else if (propertyType == Long.class || propertyType == long.class) {
      jdbcType = JdbcType.BIGINT;
    } else if (propertyType == Float.class || propertyType == float.class) {
      jdbcType = JdbcType.FLOAT;
    } else if (propertyType == Double.class || propertyType == double.class) {
      jdbcType = JdbcType.DOUBLE;
    } else if (propertyType == char.class || propertyType == Character.class) {
      jdbcType = JdbcType.CHAR;
    } else if (CharSequence.class.isAssignableFrom(propertyType)) {
      jdbcType = JdbcType.VARCHAR;
    } else if (propertyType == LocalDateTime.class) {
      jdbcType = JdbcType.TIMESTAMP;
    } else if (propertyType == LocalDate.class) {
      jdbcType = JdbcType.DATE;
    } else if (propertyType == LocalTime.class) {
      jdbcType = JdbcType.TIME;
    } else if (propertyType == BigInteger.class) {
      jdbcType = JdbcType.BIGINT;
    } else {
      throw new IllegalArgumentException(String.format("[%s] : Unsupported property type", propertyType.getCanonicalName()));
    }
    builder.jdbcType(jdbcType);
    return builder.build();
  }

  /**
   * 针对BaseModel的一个属性创建ParameterMapping对象
   * org.apache.ibatis.type.JdbcType
   */
  private ParameterMapping buildParameterMapping(JdbcType jdbcType, String fieldName, Configuration configuration) {
    // 没有指定propertyType，调用的是UnknownTypeHandler。在UnknownTypeHandler里能拿到propertyType。再获取真正的TypeHandler
    ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, fieldName, Object.class);
    builder.jdbcType(jdbcType);
    return builder.build();
  }

  private void fillInsert(BaseModel baseModel, Configuration configuration) {
    List<String> allFieldNameList = baseModel.getAllFieldNameList();
    MetaObject metaObject = configuration.newMetaObject(baseModel);
    for (String fieldName : allFieldNameList) {
      if (baseModelSource.ifValue(fieldName)) {
        metaObject.setValue(fieldName, baseModelSource.getValue(fieldName, baseModel));
      }
    }
  }

  private void fillUpdate(BaseModel baseModel, Configuration configuration) {
    List<String> updateFieldNameList = baseModel.getUpdateFieldNameList();
    MetaObject metaObject = configuration.newMetaObject(baseModel);
    for (String fieldName : updateFieldNameList) {
      if (baseModelSource.ifValue(fieldName)) {
        metaObject.setValue(fieldName, baseModelSource.getValue(fieldName, baseModel));
      }
    }
  }

  private List<String> formatColumnName(List<Column> columnList) {
    return columnList.stream()
            .map(Column::getColumnName)
            .map(this::removeGrave)
            .collect(Collectors.toList());
  }

  /**
   * todo 应该使用SQL解析器获取JdbcParameter的
   * <p>
   * Count the occurrences of the substring {@code sub} in string {@code str}.
   *
   * @param str string to search in
   * @param sub string to search for
   */
  private int countOccurrencesOf(String str, String sub) {
    if (str == null || str.isEmpty() || sub == null || sub.isEmpty()) {
      return 0;
    }

    int count = 0;
    int pos = 0;
    int idx;
    while ((idx = str.indexOf(sub, pos)) != -1) {
      ++count;
      pos = idx + sub.length();
    }
    return count;
  }

  private String removeGrave(String str) {
    if (str == null) {
      return null;
    }
    if ("`".equals(str)) {
      return str;
    }
    int start = 0;
    int end = str.length();
    if (str.startsWith("`")) {
      start = start + 1;
    }
    if (str.endsWith("`")) {
      end = end - 1;
    }
    if (start != 0 || end != str.length()) {
      return str.substring(start, end);
    }
    return str;
  }

  private String camelCaseToSnakeCase(String camelCase) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < camelCase.length(); i++) {
      char ch = camelCase.charAt(i);
      if (Character.isUpperCase(ch)) {
        builder.append("_");
      }
      builder.append(Character.toLowerCase(ch));
    }
    return builder.toString();
  }

}
