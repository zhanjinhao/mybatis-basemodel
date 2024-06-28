package cn.addenda.mybatisbasemodel.simple;

import cn.addenda.mybatisbasemodel.core.BaseModel;
import cn.addenda.mybatisbasemodel.core.BaseModelSource;

public class SimpleBaseModelSource implements BaseModelSource {

  public static final String USER_EL = "T(cn.addenda.mybatisbasemodel.simple.SimpleBaseModelSource).getUser()";

  private static final ThreadLocal<String> USER_SOURCE_TL = ThreadLocal.withInitial(() -> null);

  public static void setUser(String user) {
    USER_SOURCE_TL.set(user);
  }

  public static void removeUser() {
    USER_SOURCE_TL.remove();
  }

  public static String getUser() {
    return USER_SOURCE_TL.get();
  }

  public static final String HOST_EL = "T(cn.addenda.mybatisbasemodel.simple.SimpleBaseModelSource).getHost()";

  private static final ThreadLocal<String> HOST_SOURCE_TL = ThreadLocal.withInitial(() -> null);

  public static void setHost(String user) {
    HOST_SOURCE_TL.set(user);
  }

  public static void removeHost() {
    HOST_SOURCE_TL.remove();
  }

  public static String getHost() {
    return HOST_SOURCE_TL.get();
  }


  @Override
  public Object getValue(String fieldName, BaseModel model) {
    if (SimpleBaseModel.F_CREATOR.equals(fieldName) || SimpleBaseModel.F_MODIFIER.equals(fieldName)
            || SimpleBaseModel.F_CREATOR_NAME.equals(fieldName) || (SimpleBaseModel.F_MODIFIER_NAME.equals(fieldName))) {
      return USER_SOURCE_TL.get();
    }
    return null;
  }

  @Override
  public String getExpression(String fieldName, BaseModel baseModel) {
    return "now(3)";
  }

  @Override
  public boolean ifValue(String fieldName) {
    if (SimpleBaseModel.F_CREATE_TIME.equals(fieldName) || (SimpleBaseModel.F_MODIFY_TIME.equals(fieldName))) {
      return false;
    }
    return true;
  }


  public static void runWithHost(String host, Runnable runnable) {
    try {
      setHost(host);
      runnable.run();
    } finally {
      removeHost();
    }
  }

  public static void runWithUser(String user, Runnable runnable) {
    try {
      SimpleBaseModelSource.setUser(user);
      runnable.run();
    } finally {
      SimpleBaseModelSource.removeUser();
    }
  }

}
