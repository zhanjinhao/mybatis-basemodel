package cn.addenda.mybatisbasemodel.core.util;

import cn.addenda.mybatisbasemodel.core.JSqlParserStatementWrapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;

import java.util.List;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JSqlParserUtils {

  public static Statement parseAndGetStatement(String sql) {
    if (sql == null) {
      return null;
    }

    try {
      return CCJSqlParserUtil.parse(sql);
    } catch (JSQLParserException e) {
      log.error("can not parse [{}] to statement.", sql, e);
      return null;
    }
  }

  public static Expression parseExpression(String expression) {
    if (expression == null) {
      return null;
    }

    try {
      return CCJSqlParserUtil.parseExpression(expression);
    } catch (JSQLParserException e) {
      log.error("can not parse [{}] to expression.", expression, e);
      return null;
    }
  }

  public static JSqlParserStatementWrapper wrap(Statement statement) {
    return new JSqlParserStatementWrapper(statement);
  }

}
