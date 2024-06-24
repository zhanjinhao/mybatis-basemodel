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

//  @Test
//  void test2() {
//    ExpressionParser parser = new SpelExpressionParser();
//    Expression exp = parser.parseExpression("#list[0]+#list[1]");
//    StandardEvaluationContext context = new StandardEvaluationContext();
//    context.setVariable("name", "addenda");
//    context.setVariable("age", 1);
//    List<Object> objectList = new ArrayList<>();
//    objectList.add("addenda");
//    objectList.add(1);
//    context.setVariable("list", objectList);
//    System.out.println(exp.getValue(context));
//  }
//
//  @Test
//  public void test3() {
//    ExpressionParser parser = new SpelExpressionParser();
//    Expression exp = parser.parseExpression("#list");
//    StandardEvaluationContext context = new StandardEvaluationContext();
//    context.setVariable("name", "addenda");
//    context.setVariable("age", 1);
//    List<Object> objectList = new ArrayList<>();
//    objectList.add("addenda");
//    objectList.add(1);
//    context.setVariable("list", objectList);
//    System.out.println(exp.getValue(context));
//  }
//
//  @Test
//  public void test4() {
//    ExpressionParser parser = new SpelExpressionParser();
//    Expression exp = parser.parseExpression("T(com.sf.sfa.common.utils.MD5Utils).md5(#spELArgs[0])");
//    List<String> list = new ArrayList<>();
//    list.add("123");
//    StandardEvaluationContext context = new StandardEvaluationContext();
//    context.setVariable("spELArgs", list);
//    System.out.println(exp.getValue(context));
//  }
//
//  @Test
//  public void test5() {
//    ExpressionParser parser = new SpelExpressionParser();
//    Expression exp = parser.parseExpression(SpELUtils.MD5);
//    List<String> list = new ArrayList<>();
//    list.add("123");
//    StandardEvaluationContext context = new StandardEvaluationContext();
//    context.setVariable("spELArgs", list);
//    System.out.println(exp.getValue(context));
//  }
//
//  @Test
//  public void test6() {
//    UserUtils.setManualDefinedUserId("01395265");
//    try {
//      ExpressionParser parser = new SpelExpressionParser();
//      Expression exp = parser.parseExpression(SpELUtils.USER_ID);
//      List<String> list = new ArrayList<>();
//      list.add("123");
//      StandardEvaluationContext context = new StandardEvaluationContext();
//      context.setVariable("spELArgs", list);
//      System.out.println(exp.getValue(context));
//    } finally {
//      UserUtils.removeManualDefinedUserId();
//    }
//  }
//
//  @Test
//  public void test7() {
//    UserDto userDto = new UserDto();
//    System.out.println(SpELUtils.getObject("#this", userDto));
//  }
//
//  @Test
//  public void test8() {
//    System.out.println(SpELUtils.getObject(SpELUtils.USER_ID, null));
//  }
//
//  @Test
//  public void test9() {
//    System.out.println(SpELUtils.getObject(ParamInjectionConstant.EL_USER_NAME, null));
//    System.out.println(SpELUtils.getObject(ParamInjectionConstant.EL_USER_ID, null));
//    System.out.println(SpELUtils.getObject(ParamInjectionConstant.EL_LOCAL_DATETIME_NOW, null));
//    System.out.println(SpELUtils.getObject(ParamInjectionConstant.EL_DATE_NOW, null));
//  }

}
