package cn.addenda.mybatisbasemodel.core.util;

import cn.addenda.mybatisbasemodel.core.BaseModelException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InstanceUtils {

  public static <T> T newInstance(String clazzName, Class<T> tClass) {
    try {
      Class<?> aClass = Class.forName(clazzName);
      if (!tClass.isAssignableFrom(aClass)) {
        String msg = String.format("new instance failed, expected type: [%s], actual type: [%s].", tClass.getName(), aClass);
        throw new BaseModelException(msg);
      }

      // 如果存在单例方法，优先取单例方法。
      Method[] methods = aClass.getMethods();
      for (Method method : methods) {
        if (method.getName().equals("getInstance") && Modifier.isStatic(method.getModifiers()) &&
                method.getParameterCount() == 0 && tClass.isAssignableFrom(method.getReturnType())) {
          return (T) method.invoke(null);
        }
      }

      // 如果不存在单例方法，取默认构造函数
      return (T) aClass.newInstance();
    } catch (Exception e) {
      String msg = String.format("new instance failed, expected type: [%s], actual type: [%s].", tClass.getName(), clazzName);
      throw new BaseModelException(msg, e);
    }
  }

}
