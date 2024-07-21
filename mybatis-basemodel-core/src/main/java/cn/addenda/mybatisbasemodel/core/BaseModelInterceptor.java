package cn.addenda.mybatisbasemodel.core;

import cn.addenda.mybatisbasemodel.core.util.*;
import cn.addenda.mybatisbasemodel.core.wrapper.AdditionWrapper;
import cn.addenda.mybatisbasemodel.core.wrapper.BaseModelAdditionWrapper;
import cn.addenda.mybatisbasemodel.core.wrapper.PojoAdditionWrapper;
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
import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

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
    if (!BaseModelContext.ifEnable()) {
      return invocation.proceed();
    }

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
        if (parameterObject instanceof AdditionWrapper) {
          rewriteInsertSql(statementHandlerMetaObject, mappedStatement, (AdditionWrapper<?>) parameterObject);
        }
      } else if (sqlCommandType == SqlCommandType.UPDATE) {
        Object parameterObject = boundSql.getParameterObject();
        if (parameterObject instanceof BaseModel) {
          rewriteUpdateSql(statementHandlerMetaObject, mappedStatement, (BaseModel) parameterObject);
        }
        if (parameterObject instanceof AdditionWrapper) {
          rewriteUpdateSql(statementHandlerMetaObject, mappedStatement, (AdditionWrapper<?>) parameterObject);
        }
      }
    } else if ("parameterize".equals(method.getName())) {
      if (sqlCommandType == SqlCommandType.INSERT) {
        Object parameterObject = boundSql.getParameterObject();
        if (parameterObject instanceof BaseModel) {
          injectInsertSql(statementHandlerMetaObject, mappedStatement, (BaseModel) parameterObject);
        }
        if (parameterObject instanceof AdditionWrapper) {
          injectInsertSql(statementHandlerMetaObject, mappedStatement, (AdditionWrapper<?>) parameterObject);
        }
      } else if (sqlCommandType == SqlCommandType.UPDATE) {
        Object parameterObject = boundSql.getParameterObject();
        if (parameterObject instanceof BaseModel) {
          injectUpdateSql(statementHandlerMetaObject, mappedStatement, (BaseModel) parameterObject);
        }
        if (parameterObject instanceof AdditionWrapper) {
          injectUpdateSql(statementHandlerMetaObject, mappedStatement, (AdditionWrapper<?>) parameterObject);
        }
      }
    } else {
      throw new UnsupportedOperationException(method.toString());
    }
    return invocation.proceed();
  }

  private void rewriteInsertSql(MetaObject statementHandlerMetaObject, MappedStatement mappedStatement, AdditionWrapper<?> additionWrapper) {
    StatementHandler statementHandler = (StatementHandler) statementHandlerMetaObject.getOriginalObject();
    BoundSql boundSql = statementHandler.getBoundSql();
    Insert insert = parseAndGetStatement(statementHandler, Insert.class);

    String newSql = doRewriteSql(mappedStatement, additionWrapper, JSqlParserUtils.wrap(insert));
    replaceSql(statementHandlerMetaObject, boundSql, mappedStatement, newSql);
  }

  private void rewriteUpdateSql(MetaObject statementHandlerMetaObject, MappedStatement mappedStatement, AdditionWrapper<?> additionWrapper) {
    StatementHandler statementHandler = (StatementHandler) statementHandlerMetaObject.getOriginalObject();
    BoundSql boundSql = statementHandler.getBoundSql();
    Update update = parseAndGetStatement(statementHandler, Update.class);

    String newSql = doRewriteSql(mappedStatement, additionWrapper, JSqlParserUtils.wrap(update));
    replaceSql(statementHandlerMetaObject, boundSql, mappedStatement, newSql);
  }

  private String doRewriteSql(MappedStatement mappedStatement, AdditionWrapper<?> additionWrapper,
                              JSqlParserStatementWrapper jSqlParserStatementWrapper) {
    List<Column> columnList = jSqlParserStatementWrapper.getColumnList();
    Map<String, AdditionAttr> additionMap = additionWrapper.getInjectedAdditionAttrMap();
    List<String> columnNameList = formatColumnName(columnList);
    for (Map.Entry<String, AdditionAttr> entry : additionMap.entrySet()) {
      AdditionAttr additionAttr = entry.getValue();
      String columnName = calculateColumnName(additionAttr, columnNameList);
      if (columnName == null) {
        continue;
      }

      if (!additionAttr.isIfValue()) {
        String expressionStr;
        if (additionAttr.isExpressionPreEvaluate()) {
          Object evaluate = additionAttr.getOrEvaluate(additionWrapper.getOriginalParam(), baseModelELEvaluator::evaluate);
          if (evaluate instanceof String) {
            expressionStr = (String) evaluate;
          } else {
            throw new BaseModelException(String.format("The result of expression evaluation is not of type String. expression:%s, result:[%s].", Arrays.toString(additionAttr.getExpression()), evaluate));
          }
        } else {
          String[] expression = additionAttr.getExpression();
          if (expression.length == 1) {
            expressionStr = expression[0];
          } else {
            throw new BaseModelException(String.format("The length of expression array can only be 1. expression:%s.", Arrays.toString(expression)));
          }
        }
        Expression expression = JSqlParserUtils.parseExpression(expressionStr);
        if (expression == null) {
          throw new UnsupportedOperationException(
                  String.format("mappedStatement [%s], can not parse expression from [%s], current name is [%s].",
                          mappedStatement.getId(), expressionStr, additionAttr.getName()));
        }
        jSqlParserStatementWrapper.addColumn(new Column(columnName), expression);
      } else {
        jSqlParserStatementWrapper.addColumn(new Column(columnName), new JdbcParameter());
      }
    }

    return jSqlParserStatementWrapper.toString();
  }

  private void injectInsertSql(MetaObject statementHandlerMetaObject, MappedStatement mappedStatement, AdditionWrapper<?> additionWrapper) {
    StatementHandler statementHandler = (StatementHandler) statementHandlerMetaObject.getOriginalObject();
    Insert insert = parseAndGetStatement2(statementHandler, Insert.class);

    List<ParameterMapping> injectedParameterMappingList = generateParameterMapping(mappedStatement, additionWrapper, JSqlParserUtils.wrap(insert));
    replaceParameterMapping(statementHandler, mappedStatement, injectedParameterMappingList, 0);
  }

  private void injectUpdateSql(MetaObject statementHandlerMetaObject, MappedStatement mappedStatement, AdditionWrapper<?> additionWrapper) {
    StatementHandler statementHandler = (StatementHandler) statementHandlerMetaObject.getOriginalObject();
    Update update = parseAndGetStatement2(statementHandler, Update.class);

    List<ParameterMapping> injectedParameterMappingList = generateParameterMapping(mappedStatement, additionWrapper, JSqlParserUtils.wrap(update));
    replaceParameterMapping(statementHandler, mappedStatement, injectedParameterMappingList, JSqlParserUtils.countJdbcParameter(update.getWhere()));
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
          parameterMappingList.add(insertIndex++, parameterMapping);
        }
        MetaObject metaObject = mappedStatement.getConfiguration().newMetaObject(boundSql);
        metaObject.setValue("parameterMappings", parameterMappingList);
      } else if (sqlSource instanceof DynamicSqlSource) {
        for (ParameterMapping parameterMapping : injectedParameterMappingList) {
          currentParameterMappingList.add(insertIndex++, parameterMapping);
        }
      }
    }
  }

  private List<ParameterMapping> generateParameterMapping(MappedStatement mappedStatement, AdditionWrapper<?> additionWrapper,
                                                          JSqlParserStatementWrapper jSqlParserStatementWrapper) {
    Configuration configuration = mappedStatement.getConfiguration();

    List<Column> columnList = jSqlParserStatementWrapper.getColumnList();
    Map<String, AdditionAttr> additionMap = additionWrapper.getInjectedAdditionAttrMap();
    List<String> columnNameList = formatColumnName(columnList);
    List<ParameterMapping> parameterMappingList = new ArrayList<>();
    for (Map.Entry<String, AdditionAttr> entry : additionMap.entrySet()) {
      AdditionAttr additionAttr = entry.getValue();
      String columnName = calculateColumnName(additionAttr, columnNameList);
      if (columnName == null) {
        continue;
      }

      if (additionAttr.isIfValue()) {
        ParameterMapping parameterMapping = buildParameterMapping(additionAttr.getJdbcType(), additionAttr.getName(), configuration);
        parameterMappingList.add(parameterMapping);
      }
    }
    return parameterMappingList;
  }

  private void rewriteInsertSql(MetaObject statementHandlerMetaObject, MappedStatement mappedStatement, BaseModel baseModel) {
    StatementHandler statementHandler = (StatementHandler) statementHandlerMetaObject.getOriginalObject();
    BoundSql boundSql = statementHandler.getBoundSql();
    Insert insert = parseAndGetStatement(statementHandler, Insert.class);

    String newSql = doRewriteSql(mappedStatement, BaseModelMetaDataUtils.getAllFieldNameList(baseModel), baseModel, JSqlParserUtils.wrap(insert));
    replaceSql(statementHandlerMetaObject, boundSql, mappedStatement, newSql);
  }

  private void rewriteUpdateSql(MetaObject statementHandlerMetaObject, MappedStatement mappedStatement, BaseModel baseModel) {
    StatementHandler statementHandler = (StatementHandler) statementHandlerMetaObject.getOriginalObject();
    BoundSql boundSql = statementHandler.getBoundSql();
    Update update = parseAndGetStatement(statementHandler, Update.class);

    String newSql = doRewriteSql(mappedStatement, BaseModelMetaDataUtils.getUpdateFieldNameList(baseModel), baseModel, JSqlParserUtils.wrap(update));
    replaceSql(statementHandlerMetaObject, boundSql, mappedStatement, newSql);
  }

  private String doRewriteSql(MappedStatement mappedStatement, List<String> injectedFieldNameList, BaseModel baseModel,
                              JSqlParserStatementWrapper jSqlParserStatementWrapper) {
    List<Column> columnList = jSqlParserStatementWrapper.getColumnList();
    List<String> columnNameList = formatColumnName(columnList);

    for (String fieldName : injectedFieldNameList) {
      Field field = BaseModelMetaDataUtils.getFieldByFieldName(baseModel, fieldName);
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

    List<ParameterMapping> injectedParameterMappings = generateParameterMapping(mappedStatement, BaseModelMetaDataUtils.getAllFieldNameList(baseModel), baseModel, JSqlParserUtils.wrap(insert));
    replaceParameterMapping(statementHandler, mappedStatement, injectedParameterMappings, 0);
  }

  private void injectUpdateSql(MetaObject statementHandlerMetaObject, MappedStatement mappedStatement, BaseModel baseModel) {
    StatementHandler statementHandler = (StatementHandler) statementHandlerMetaObject.getOriginalObject();
    Update update = parseAndGetStatement2(statementHandler, Update.class);

    List<ParameterMapping> injectedParameterMappings = generateParameterMapping(mappedStatement, BaseModelMetaDataUtils.getUpdateFieldNameList(baseModel), baseModel, JSqlParserUtils.wrap(update));
    replaceParameterMapping(statementHandler, mappedStatement, injectedParameterMappings, JSqlParserUtils.countJdbcParameter(update.getWhere()));
  }

  private List<ParameterMapping> generateParameterMapping(MappedStatement mappedStatement, List<String> injectedFieldNameList, BaseModel baseModel,
                                                          JSqlParserStatementWrapper jSqlParserStatementWrapper) {
    Configuration configuration = mappedStatement.getConfiguration();
    List<Column> columnList = jSqlParserStatementWrapper.getColumnList();
    List<String> columnNameList = formatColumnName(columnList);

    List<ParameterMapping> parameterMappingList = new ArrayList<>();
    for (String fieldName : injectedFieldNameList) {
      Field field = BaseModelMetaDataUtils.getFieldByFieldName(baseModel, fieldName);
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
    String columnName = BaseModelMetaDataUtils.getColumnName(field);

    // 属性去重，对于用户已经添加的字段，不能再自动注入
    if (columnNameList.contains(columnName)) {
      return null;
    }

    return columnName;
  }

  private String calculateColumnName(AdditionAttr additionAttr, List<String> columnNameList) {
    String columnName = BaseModelMetaDataUtils.getColumnName(additionAttr);

    // 属性去重，对于用户已经添加的字段，不能再自动注入
    if (columnNameList.contains(columnName)) {
      return null;
    }

    return columnName;
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

    if (!(parameterObject instanceof AdditionWrapper)) {
      return invocation.proceed();
    }

    BoundSql boundSql = (BoundSql) metaObject.getValue("boundSql");

    AdditionWrapper<?> additionWrapper = (AdditionWrapper<?>) parameterObject;
    List<AdditionAttr> additionAttrList = additionWrapper.getInjectedAdditionAttrList();
    try {
      for (AdditionAttr additionAttr : additionAttrList) {
        if (additionAttr.isIfValue()) {
          boundSql.setAdditionalParameter(additionAttr.getName(),
                  additionAttr.getOrEvaluate(additionWrapper.getOriginalParam(), baseModelELEvaluator::evaluate));
        }
      }
      boundSql.setAdditionalParameter(AdditionWrapper.ORIGINAL_PARAM_NAME, additionWrapper.getOriginalParam());
      return invocation.proceed();
    } finally {
      for (AdditionAttr additionAttr : additionAttrList) {
        if (additionAttr.isIfValue()) {
          boundSql.getAdditionalParameters().remove(additionAttr.getName());
        }
      }
      boundSql.getAdditionalParameters().remove(AdditionWrapper.ORIGINAL_PARAM_NAME);
    }
  }

  private Object interceptExecutor(Executor executor, Invocation invocation) throws Throwable {
    Object[] args = invocation.getArgs();
    MappedStatement mappedStatement = (MappedStatement) args[0];
    String msId = mappedStatement.getId();

    List<AdditionAttr> additionAttrList = MsIdAnnotationUtils.extractAddition(msId, mappedStatement.getSqlCommandType());
    if (!additionAttrList.isEmpty()) {
      // 观察org.apache.ibatis.reflection.ParamNameResolver.getNamedParams的实现可知，返回值一共有三种类型
      // - 没有参数时返回null
      // - 有一个参数且没有@Param注解时返回参数本身
      // - 返回MapperMethod.ParamMap
      Object arg = args[1];
      if (arg == null) {
        AdditionWrapper<?> paramWrapper = new AdditionWrapper<>(baseModelELEvaluator, null, additionAttrList);
        paramWrapper.init();
        args[1] = paramWrapper;
      } else {
        if (arg instanceof MapperMethod.ParamMap) {
          AdditionWrapper<?> paramWrapper = new AdditionWrapper<>(baseModelELEvaluator, arg, additionAttrList);
          paramWrapper.putAll((MapperMethod.ParamMap<?>) arg);
          paramWrapper.init();
          args[1] = paramWrapper;
        }
        // StrictMap在3.5.5之前是 org.apache.ibatis.session.defaults.DefaultSqlSession.wrapCollection() 的返回值。兼容一下。
        else if (arg instanceof DefaultSqlSession.StrictMap) {
          AdditionWrapper<?> paramWrapper = new AdditionWrapper<>(baseModelELEvaluator, arg, additionAttrList);
          paramWrapper.putAll((DefaultSqlSession.StrictMap<?>) arg);
          paramWrapper.init();
          args[1] = paramWrapper;
        }
        // 一个参数且没有@Param时走这个地方
        else {
          TypeHandlerRegistry typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
          if (typeHandlerRegistry.hasTypeHandler(arg.getClass())) {
            AdditionWrapper<?> paramWrapper = new AdditionWrapper<>(baseModelELEvaluator, arg, additionAttrList);
            paramWrapper.setFallback(true);
            paramWrapper.init();
            args[1] = paramWrapper;
          } else if (arg instanceof BaseModel) {
            BaseModel baseModel = (BaseModel) arg;
            BaseModelAdditionWrapper paramWrapper = new BaseModelAdditionWrapper(baseModelELEvaluator, baseModel, additionAttrList);
            paramWrapper.init();
            replaceKeyGenerator(executor, mappedStatement);
            args[1] = paramWrapper;
          } else {
            PojoAdditionWrapper<?> paramWrapper = new PojoAdditionWrapper<>(baseModelELEvaluator, arg, additionAttrList);
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
   */
  private ParameterMapping buildParameterMapping(Field field, String fieldName, Configuration configuration) {
    if (field == null) {
      throw new NullPointerException(String.format("field [%s] 为空！", fieldName));
    }
    Class<?> propertyType = field.getType();
    ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, fieldName, propertyType);
    builder.jdbcType(MsIdAnnotationUtils.calculateJdbcType(field));
    return builder.build();
  }

  /**
   * 针对AdditionAttr创建ParameterMapping对象
   */
  private ParameterMapping buildParameterMapping(JdbcType jdbcType, String fieldName, Configuration configuration) {
    // 没有指定propertyType，调用的是UnknownTypeHandler。在UnknownTypeHandler里能拿到propertyType。再获取真正的TypeHandler
    ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, fieldName, Object.class);
    builder.jdbcType(jdbcType);
    return builder.build();
  }

  private void fillInsert(BaseModel baseModel, Configuration configuration) {
    List<String> allFieldNameList = BaseModelMetaDataUtils.getAllFieldNameList(baseModel);
    doFill(allFieldNameList, baseModel, configuration);
  }

  private void fillUpdate(BaseModel baseModel, Configuration configuration) {
    List<String> updateFieldNameList = BaseModelMetaDataUtils.getUpdateFieldNameList(baseModel);
    doFill(updateFieldNameList, baseModel, configuration);
  }

  private void doFill(List<String> fieldNameList, BaseModel baseModel, Configuration configuration) {
    MetaObject metaObject = configuration.newMetaObject(baseModel);
    short fillMode = BaseModelContext.getFillMode();
    if (fillMode == BaseModelContext.FILL_MODE_SKIP) {
      return;
    }
    for (String fieldName : fieldNameList) {
      if (!baseModelSource.ifValue(fieldName)) {
        continue;
      }
      if (fillMode == BaseModelContext.FILL_MODE_FORCE) {
        metaObject.setValue(fieldName, baseModelSource.getValue(fieldName, baseModel));
      } else if (fillMode == BaseModelContext.FILL_MODE_NULL) {
        if (metaObject.getValue(fieldName) == null) {
          metaObject.setValue(fieldName, baseModelSource.getValue(fieldName, baseModel));
        }
      } else if (fillMode == BaseModelContext.FILL_MODE_EMPTY) {
        Object value = metaObject.getValue(fieldName);
        if (value == null || ("".equals(value))) {
          metaObject.setValue(fieldName, baseModelSource.getValue(fieldName, baseModel));
        }
      } else {
        throw new IllegalArgumentException("unsupported fill mode : %s");
      }
    }
  }

  private List<String> formatColumnName(List<Column> columnList) {
    return columnList.stream()
            .map(Column::getColumnName)
            .map(this::removeGrave)
            .collect(Collectors.toList());
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

}
