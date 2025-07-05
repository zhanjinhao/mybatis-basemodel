package cn.addenda.mybatisbasemodel.core.util;

import cn.addenda.mybatisbasemodel.core.AdditionAttr;
import cn.addenda.mybatisbasemodel.core.BaseModel;
import cn.addenda.mybatisbasemodel.core.BaseModelException;
import cn.addenda.mybatisbasemodel.core.annotation.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.JdbcType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BaseModelMetaDataUtils {

  private static final Map<Class<?>, Map<String, Field>> allFieldMap = new ConcurrentHashMap<>();
  private static final Map<Class<?>, Map<String, Field>> updateFieldMap = new ConcurrentHashMap<>();

  public static <T extends BaseModel> Map<String, Field> getAllFieldMap(Class<T> clazz) {
    return allFieldMap.computeIfAbsent(clazz, aClass -> {
      Map<String, Field> fieldMap = new HashMap<>();
      List<Field> declaredFieldList = getAllField(aClass);
      for (Field field : declaredFieldList) {
        if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())
                || !field.isAnnotationPresent(InsertField.class)) {
          continue;
        }
        fieldMap.put(field.getName(), field);
      }
      return Collections.unmodifiableMap(fieldMap);
    });
  }

  public static <T extends BaseModel> Map<String, Field> getUpdateFieldMap(Class<T> clazz) {
    return updateFieldMap.computeIfAbsent(clazz, aClass -> {
      Map<String, Field> fieldMap = new HashMap<>();
      List<Field> declaredFieldList = getAllField(aClass);
      for (Field field : declaredFieldList) {
        if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())
                || !field.isAnnotationPresent(UpdateField.class)) {
          continue;
        }
        fieldMap.put(field.getName(), field);
      }
      return fieldMap;
    });
  }

  private static List<Field> getAllField(Class<?> clazz) {
    List<Field> fieldList = new ArrayList<>();
    Class<?> c = clazz;
    while (c != null && !BaseModel.class.equals(c)) {
      Collections.addAll(fieldList, c.getDeclaredFields());
      c = c.getSuperclass();
    }
    return fieldList;
  }

  public static String getExpression(Field field) {
    BaseModelExpression annotation = field.getAnnotation(BaseModelExpression.class);
    if (annotation == null) {
      throw new BaseModelException(String.format("can not extract [%s] from [%s].", BaseModelExpression.class, field));
    }
    return annotation.expression();
  }

  public static boolean getIfObj(Field field) {
    BaseModelExpression annotation = field.getAnnotation(BaseModelExpression.class);
    if (annotation == null) {
      throw new BaseModelException(String.format("can not extract [%s] from [%s].", BaseModelExpression.class, field));
    }
    return annotation.ifObj();
  }

  public static String getColumnName(Field field) {
    BaseModelColumnName annotation = field.getAnnotation(BaseModelColumnName.class);
    if (annotation == null) {
      return camelCaseToSnakeCase(field.getName());
    }
    return annotation.value();
  }

  public static String getColumnName(AdditionAttr additionAttr) {
    String columnName = additionAttr.getColumnName();
    if (AdditionAttr.BASE_MODEL_COLUMN.equals(columnName)) {
      String fieldName = additionAttr.getName();
      columnName = camelCaseToSnakeCase(fieldName);
    }
    return columnName;
  }

  public static JdbcType getJdbcType(Field field) {
    BaseModelJdbcType annotation = field.getAnnotation(BaseModelJdbcType.class);
    if (annotation == null) {
      return null;
    }
    return annotation.value();
  }

  private static String camelCaseToSnakeCase(String camelCase) {
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
