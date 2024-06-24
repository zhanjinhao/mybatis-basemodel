package cn.addenda.mybatisbasemodel.core;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Values;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import org.junit.jupiter.api.Test;

import java.util.List;

class TestJSqlParser {

  @Test
  void testInsertValue() throws Exception {
    String sql = "insert into t (name, age) values ('a', ?)";
    Statement parse = CCJSqlParserUtil.parse(sql);

    Insert insert = (Insert) parse;
    ExpressionList<Column> columns = insert.getColumns();

    Values values = (Values) insert.getSelect();
    ExpressionList<?> expressions = values.getExpressions();

    System.out.println(parse);
  }

  @Test
  void testInsertSet() throws Exception {
    String sql = "insert into t set name = 'a', age = ?";
    Statement parse = CCJSqlParserUtil.parse(sql);

    System.out.println(parse);
    Insert insert = (Insert) parse;
    List<UpdateSet> setUpdateSets = insert.getSetUpdateSets();

  }

  @Test
  void testUpdate() throws Exception {
    String sql = "update t set name = 'a', age = ?, create_tm = now(3) where id = ?";
    Statement parse = CCJSqlParserUtil.parse(sql);

    System.out.println(parse);

    Update update = (Update) parse;

    List<UpdateSet> updateSets = update.getUpdateSets();
  }

  @Test
  void testFunction() throws Exception {
    String sql = "now(3)";
    Expression expression = CCJSqlParserUtil.parseExpression(sql);

    System.out.println(expression);
  }

}
