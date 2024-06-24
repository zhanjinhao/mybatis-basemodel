package cn.addenda.mybatisbasemodel.spring;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * @author addenda
 * @since 2023/7/29 19:08
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpELUtils {

  private static final ExpressionParser SP_EL_PARSER = new SpelExpressionParser();

  public static Object getObject(String spEL, Object argument) {
    Expression exp = SP_EL_PARSER.parseExpression(spEL);
    StandardEvaluationContext context = new StandardEvaluationContext(argument);
    return exp.getValue(context);
  }

  public static Object getObjectIgnoreException(String spEL, Object argument) {
    try {
      Expression exp = SP_EL_PARSER.parseExpression(spEL);
      StandardEvaluationContext context = new StandardEvaluationContext(argument);
      return exp.getValue(context);
    } catch (Exception e) {
      log.debug("SpringEL invoke failed，spEL[{}]，argument[{}]。", spEL, argument);
      return null;
    }
  }

  public static <T> T getObject(String spEL, Object argument, Class<T> returnClass) {
    Expression exp = SP_EL_PARSER.parseExpression(spEL);
    StandardEvaluationContext context = new StandardEvaluationContext(argument);
    return exp.getValue(context, returnClass);
  }

  public static <T> T getObjectIgnoreException(String spEL, Object argument, Class<T> returnClass) {
    try {
      Expression exp = SP_EL_PARSER.parseExpression(spEL);
      StandardEvaluationContext context = new StandardEvaluationContext(argument);
      return exp.getValue(context, returnClass);
    } catch (Exception e) {
      log.debug("SpringEL invoke failed，spEL[{}]，argument[{}]，returnClass[{}]。", spEL, argument, returnClass);
      return null;
    }
  }

}
