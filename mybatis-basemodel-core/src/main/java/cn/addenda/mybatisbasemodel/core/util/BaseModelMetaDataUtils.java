package cn.addenda.mybatisbasemodel.core.util;

import cn.addenda.mybatisbasemodel.core.AdditionAttr;
import cn.addenda.mybatisbasemodel.core.BaseModel;
import cn.addenda.mybatisbasemodel.core.BaseModelAdapter;
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
  private static final Map<Class<?>, List<String>> allFieldNameMap = new ConcurrentHashMap<>();
  private static final Map<Class<?>, List<String>> updateFieldNameMap = new ConcurrentHashMap<>();

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

  public static <T extends BaseModel> List<String> getAllFieldNameList(Class<T> clazz) {
    return allFieldNameMap.computeIfAbsent(
            clazz, aClass -> Collections.unmodifiableList(new ArrayList<>(getAllFieldMap(clazz).keySet())));
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

  public static <T extends BaseModel> List<String> getUpdateFieldNameList(Class<T> clazz) {
    return updateFieldNameMap.computeIfAbsent(
            clazz, aClass -> Collections.unmodifiableList(new ArrayList<>(getUpdateFieldMap(clazz).keySet())));
  }

  public static List<String> getAllFieldNameList(BaseModel baseModel) {
    if (baseModel instanceof BaseModelAdapter) {
      return getAllFieldNameList(((BaseModelAdapter) baseModel).getDelegate());
    }
    return getAllFieldNameList(baseModel.getClass());
  }

  public static Map<String, Field> getAllFieldMap(BaseModel baseModel) {
    if (baseModel instanceof BaseModelAdapter) {
      return getAllFieldMap(((BaseModelAdapter) baseModel).getDelegate());
    }
    return getAllFieldMap(baseModel.getClass());
  }

  public static List<String> getUpdateFieldNameList(BaseModel baseModel) {
    if (baseModel instanceof BaseModelAdapter) {
      return getUpdateFieldNameList(((BaseModelAdapter) baseModel).getDelegate());
    }
    return getUpdateFieldNameList(baseModel.getClass());
  }

  public static Map<String, Field> getUpdateFieldMap(BaseModel baseModel) {
    if (baseModel instanceof BaseModelAdapter) {
      return getUpdateFieldMap(((BaseModelAdapter) baseModel).getDelegate());
    }
    return getUpdateFieldMap(baseModel.getClass());
  }

  public static Field getFieldByFieldName(BaseModel baseModel, String property) {
    if (baseModel instanceof BaseModelAdapter) {
      return getFieldByFieldName(((BaseModelAdapter) baseModel).getDelegate(), property);
    }
    return getAllFieldMap(baseModel).get(property);
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

  public static boolean getIfValue(Field field) {
    BaseModelExpression annotation = field.getAnnotation(BaseModelExpression.class);
    if (annotation == null) {
      throw new BaseModelException(String.format("can not extract [%s] from [%s].", BaseModelExpression.class, field));
    }
    return annotation.ifValue();
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
