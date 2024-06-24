package cn.addenda.mybatisbasemodel.core;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 包装JSqlParser的Statement对象
 */
public class JSqlParserStatementWrapper {

  private final int sqlType;

  private final Statement statement;

  public JSqlParserStatementWrapper(Statement statement) {
    this.statement = statement;
    if (statement instanceof Update) {
      sqlType = 3;
      Update update = (Update) statement;
      validUpdateSetList(update.getUpdateSets());
    } else if (statement instanceof Insert) {
      Insert insert = (Insert) statement;
      if (insert.getSetUpdateSets() != null) {
        sqlType = 2;
        validUpdateSetList(insert.getSetUpdateSets());
      } else {
        sqlType = 1;
      }
    } else {
      throw new UnsupportedOperationException(String.format("Unsupported sql: [%s].", statement));
    }
  }

  private void validUpdateSetList(List<UpdateSet> updateSetList) {
    for (UpdateSet updateSet : updateSetList) {
      if (updateSet.getColumns() == null || updateSet.getColumns().size() != 1) {
        throw new UnsupportedOperationException(String.format("Unsupported sql: [%s].", statement));
      }
      if (updateSet.getValues() == null || updateSet.getValues().size() != 1) {
        throw new UnsupportedOperationException(String.format("Unsupported sql: [%s].", statement));
      }
    }
  }

  @Override
  public String toString() {
    if (statement == null) {
      return "null";
    }
    return statement.toString();
  }

  public void addColumn(Column column, Expression expression) {
    if (sqlType == 1) {
      Insert insert = (Insert) statement;
      insert.addColumns(column);
      insert.getValues().addExpressions(expression);
    } else if (sqlType == 2) {
      Insert insert = (Insert) statement;
      List<UpdateSet> updateSetList = insert.getSetUpdateSets();
      updateSetList.add(new UpdateSet(column, expression));
    } else if (sqlType == 3) {
      Update update = (Update) statement;
      List<UpdateSet> updateSetList = update.getUpdateSets();
      updateSetList.add(new UpdateSet(column, expression));
    }
  }

  public List<Column> getColumnList() {
    if (sqlType == 1) {
      Insert insert = (Insert) statement;
      return new ArrayList<>(insert.getColumns());
    } else if (sqlType == 2) {
      Insert insert = (Insert) statement;
      List<UpdateSet> updateSetList = insert.getSetUpdateSets();
      return updateSetList.stream().map(a -> a.getColumn(0)).collect(Collectors.toList());
    } else if (sqlType == 3) {
      Update update = (Update) statement;
      List<UpdateSet> updateSetList = update.getUpdateSets();
      return updateSetList.stream().map(a -> a.getColumn(0)).collect(Collectors.toList());
    }
    throw new UnsupportedOperationException();
  }

}
