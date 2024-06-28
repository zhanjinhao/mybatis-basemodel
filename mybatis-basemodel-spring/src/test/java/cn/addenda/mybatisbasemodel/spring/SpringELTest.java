package cn.addenda.mybatisbasemodel.spring;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * @author addenda
 * @since 2023/7/28 14:10
 */
class SpringELTest {

  @Test
  void test1() {
    Object object1 = SpELUtils.getObject("123", null);
    Assertions.assertEquals(123, object1);
    Object object2 = SpELUtils.getObject("'123'", null);
    Assertions.assertEquals("123", object2);
  }

  @Test
  void test2() {
    ExpressionParser parser = new SpelExpressionParser();
    Expression exp = parser.parseExpression("#list[0]+#list[1]");
    StandardEvaluationContext context = new StandardEvaluationContext();
    context.setVariable("name", "addenda");
    context.setVariable("age", 1);
    List<Object> objectList = new ArrayList<>();
    objectList.add("addenda");
    objectList.add(1);
    context.setVariable("list", objectList);
    Assertions.assertEquals("addenda1", exp.getValue(context));
  }

  @Test
  void test3() {
    ExpressionParser parser = new SpelExpressionParser();
    Expression exp = parser.parseExpression("#list");
    StandardEvaluationContext context = new StandardEvaluationContext();
    context.setVariable("name", "addenda");
    context.setVariable("age", 1);
    List<Object> objectList = new ArrayList<>();
    objectList.add("addenda");
    objectList.add(1);
    context.setVariable("list", objectList);
    Assertions.assertEquals(objectList, exp.getValue(context));
  }

}
