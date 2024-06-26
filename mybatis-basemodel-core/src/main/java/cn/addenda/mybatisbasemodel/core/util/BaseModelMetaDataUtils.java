package cn.addenda.mybatisbasemodel.core.util;

import cn.addenda.mybatisbasemodel.core.BaseModel;
import cn.addenda.mybatisbasemodel.core.BaseModelAdapter;
import cn.addenda.mybatisbasemodel.core.annotation.InsertField;
import cn.addenda.mybatisbasemodel.core.annotation.UpdateField;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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

}
