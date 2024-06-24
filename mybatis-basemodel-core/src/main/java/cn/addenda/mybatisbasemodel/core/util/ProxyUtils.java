package cn.addenda.mybatisbasemodel.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProxyUtils {

  public static <T> T resolveActualObject(Object handler) {
    if (!Proxy.isProxyClass(handler.getClass())) {
      return (T) handler;
    }

    InvocationHandler invocationHandler = Proxy.getInvocationHandler(handler);
    Plugin plugin = ((Plugin) invocationHandler);
    return resolveActualObject(SystemMetaObject.forObject(plugin).getValue("target"));
  }

}
